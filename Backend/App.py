# -*- coding: utf-8 -*-
"""Flask Server for Multimodal Emotion Recognition API"""

import os
import io
import torch
import torch.nn as nn
import numpy as np
import librosa
from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
from werkzeug.utils import secure_filename
from PIL import Image
import tempfile
import warnings
import cv2
warnings.filterwarnings('ignore')
from torchvision import models, transforms

# Initialize Flask app
app = Flask(__name__)
CORS(app)  # Enable CORS for Android app

# Configuration
app.config['MAX_CONTENT_LENGTH'] = 100 * 1024 * 1024  # 100MB max file size
app.config['UPLOAD_FOLDER'] = tempfile.gettempdir()
app.config['ALLOWED_EXTENSIONS'] = {'mp4', 'avi', 'mov', 'mkv', 'webm'}

# Device configuration
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(f"Using device: {device}")

# Initialize ResNet for image features
resnet = models.resnet18(pretrained=True)
resnet.fc = nn.Identity()
resnet = resnet.to(device)
resnet.eval()

# Image transform
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

# Emotion mapping
emotion_map = {
    0: "neutral",
    1: "calm",
    2: "happy",
    3: "sad",
    4: "angry",
    5: "fearful",
    6: "disgust",
    7: "surprised"
}

# Model definition (same as your code)
class LateFusionModel(nn.Module):
    def __init__(self, audio_dim=40, video_dim=3, image_dim=512, num_classes=8):
        super(LateFusionModel, self).__init__()
        fusion_dim = audio_dim + video_dim + image_dim
        
        self.fc = nn.Sequential(
            nn.Linear(fusion_dim, 256),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(256, 128),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(128, num_classes)
        )

    def forward(self, audio, video, image):
        combined = torch.cat([audio, video, image], dim=1)
        return self.fc(combined)

# Load the trained model
MODEL_PATH = "late_fusion_best.pt"
model = LateFusionModel(num_classes=8).to(device)

try:
    if os.path.exists(MODEL_PATH):
        model.load_state_dict(torch.load(MODEL_PATH, map_location=device))
    else:
        # Try to download or use default location
        print(f"Model not found at {MODEL_PATH}. Please ensure the model file exists.")
        exit(1)
except Exception as e:
    print(f"Error loading model: {e}")
    exit(1)

model.eval()
print("✅ Model loaded successfully")

# Helper functions
def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in app.config['ALLOWED_EXTENSIONS']

def extract_audio_features(video_path):
    """Extract MFCC features from video audio"""
    try:
        y, sr = librosa.load(video_path, sr=16000)
        mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=40)
        return np.mean(mfcc, axis=1)
    except Exception as e:
        print(f"Error extracting audio features: {e}")
        return np.zeros(40)

def extract_video_features(video_path):
    """Extract optical flow features from video"""
    try:
        cap = cv2.VideoCapture(video_path)
        if not cap.isOpened():
            return np.array([0.0, 0.0, 0.0])
            
        ret, prev_frame = cap.read()
        if not ret:
            return np.array([0.0, 0.0, 0.0])
            
        prev_gray = cv2.cvtColor(prev_frame, cv2.COLOR_BGR2GRAY)
        flows = []

        while True:
            ret, frame = cap.read()
            if not ret:
                break
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            flow = cv2.calcOpticalFlowFarneback(
                prev_gray, gray, None,
                0.5, 3, 15, 3, 5, 1.2, 0
            )
            flows.append(np.mean(flow))
            prev_gray = gray

        cap.release()
        
        if len(flows) == 0:
            return np.array([0.0, 0.0, 0.0])
            
        return np.array([
            np.mean(flows),
            np.std(flows),
            np.max(flows)
        ])
    except Exception as e:
        print(f"Error extracting video features: {e}")
        return np.array([0.0, 0.0, 0.0])

def extract_image_features(video_path):
    """Extract ResNet features from first video frame"""
    try:
        cap = cv2.VideoCapture(video_path)
        if not cap.isOpened():
            return np.zeros(512)
            
        ret, frame = cap.read()
        cap.release()
        
        if not ret:
            return np.zeros(512)
            
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        img = Image.fromarray(frame)
        img = transform(img).unsqueeze(0).to(device)

        with torch.no_grad():
            feat = resnet(img)

        return feat.cpu().numpy().flatten()
    except Exception as e:
        print(f"Error extracting image features: {e}")
        return np.zeros(512)

def calibrated_softmax(logits, temperature=2.5):
    """Apply temperature scaling to softmax"""
    return torch.softmax(logits / temperature, dim=1)

# API Routes
@app.route('/')
def index():
    return jsonify({
        "status": "online",
        "service": "Multimodal Emotion Recognition API",
        "endpoints": {
            "/predict": "POST - Upload video for emotion recognition",
            "/health": "GET - Check server health",
            "/model-info": "GET - Get model information"
        }
    })

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "device": str(device),
        "model_loaded": True
    })

@app.route('/model-info', methods=['GET'])
def model_info():
    """Get model information"""
    total_params = sum(p.numel() for p in model.parameters())
    trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
    
    return jsonify({
        "model_name": "LateFusionModel",
        "num_classes": 8,
        "emotions": list(emotion_map.values()),
        "total_parameters": total_params,
        "trainable_parameters": trainable_params,
        "input_dimensions": {
            "audio": 40,
            "video": 3,
            "image": 512
        }
    })

@app.route('/predict', methods=['POST'])
def predict_emotion():
    """Main prediction endpoint - accepts video file"""
    # Check if file is in the request
    if 'file' not in request.files:
        return jsonify({"error": "No file provided"}), 400
    
    file = request.files['file']
    
    # Check if file is selected
    if file.filename == '':
        return jsonify({"error": "No file selected"}), 400
    
    # Check file type
    if not allowed_file(file.filename):
        return jsonify({
            "error": "Invalid file type",
            "allowed_types": list(app.config['ALLOWED_EXTENSIONS'])
        }), 400
    
    try:
        # Save uploaded file temporarily
        filename = secure_filename(file.filename)
        temp_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(temp_path)
        
        print(f"Processing file: {filename}")
        
        # Extract features
        print("Extracting audio features...")
        audio_feat = extract_audio_features(temp_path)
        
        print("Extracting video features...")
        video_feat = extract_video_features(temp_path)
        
        print("Extracting image features...")
        image_feat = extract_image_features(temp_path)
        
        # Convert to tensors
        audio_tensor = torch.tensor(audio_feat, dtype=torch.float32).unsqueeze(0).to(device)
        video_tensor = torch.tensor(video_feat, dtype=torch.float32).unsqueeze(0).to(device)
        image_tensor = torch.tensor(image_feat, dtype=torch.float32).unsqueeze(0).to(device)
        
        # Make prediction
        with torch.no_grad():
            logits = model(audio_tensor, video_tensor, image_tensor)
            probs = calibrated_softmax(logits, temperature=2.5)
        
        pred = torch.argmax(probs, dim=1).item()
        confidence = probs[0, pred].item()
        
        # Get all probabilities
        all_probs = probs[0].cpu().numpy()
        probabilities = {emotion_map[i]: float(all_probs[i]) for i in range(len(emotion_map))}
        
        # Clean up temporary file
        if os.path.exists(temp_path):
            os.remove(temp_path)
        
        # Prepare response
        response = {
            "status": "success",
            "filename": filename,
            "prediction": {
                "emotion": emotion_map[pred],
                "emotion_id": pred,
                "confidence": round(confidence, 4)
            },
            "probabilities": probabilities,
            "features": {
                "audio_shape": audio_feat.shape,
                "video_shape": video_feat.shape,
                "image_shape": image_feat.shape
            }
        }
        
        return jsonify(response)
        
    except Exception as e:
        # Clean up if file exists
        if 'temp_path' in locals() and os.path.exists(temp_path):
            os.remove(temp_path)
            
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500

@app.route('/predict-batch', methods=['POST'])
def predict_batch():
    """Batch prediction endpoint for multiple videos"""
    if 'files' not in request.files:
        return jsonify({"error": "No files provided"}), 400
    
    files = request.files.getlist('files')
    
    if len(files) == 0:
        return jsonify({"error": "No files selected"}), 400
    
    results = []
    
    for file in files:
        if file and allowed_file(file.filename):
            try:
                # Save file temporarily
                filename = secure_filename(file.filename)
                temp_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
                file.save(temp_path)
                
                # Extract features
                audio_feat = extract_audio_features(temp_path)
                video_feat = extract_video_features(temp_path)
                image_feat = extract_image_features(temp_path)
                
                # Convert to tensors
                audio_tensor = torch.tensor(audio_feat, dtype=torch.float32).unsqueeze(0).to(device)
                video_tensor = torch.tensor(video_feat, dtype=torch.float32).unsqueeze(0).to(device)
                image_tensor = torch.tensor(image_feat, dtype=torch.float32).unsqueeze(0).to(device)
                
                # Make prediction
                with torch.no_grad():
                    logits = model(audio_tensor, video_tensor, image_tensor)
                    probs = calibrated_softmax(logits, temperature=2.5)
                
                pred = torch.argmax(probs, dim=1).item()
                confidence = probs[0, pred].item()
                
                # Clean up
                if os.path.exists(temp_path):
                    os.remove(temp_path)
                
                results.append({
                    "filename": filename,
                    "emotion": emotion_map[pred],
                    "emotion_id": pred,
                    "confidence": round(confidence, 4)
                })
                
            except Exception as e:
                results.append({
                    "filename": file.filename,
                    "error": str(e)
                })
    
    return jsonify({
        "status": "success",
        "total_files": len(files),
        "processed": len(results),
        "results": results
    })

# Error handlers
@app.errorhandler(413)
def too_large(e):
    return jsonify({"error": "File too large. Maximum size is 100MB"}), 413

@app.errorhandler(404)
def not_found(e):
    return jsonify({"error": "Endpoint not found"}), 404

@app.errorhandler(500)
def server_error(e):
    return jsonify({"error": "Internal server error"}), 500

if __name__ == '__main__':
    # Run the Flask app
    print("=" * 50)
    print("Multimodal Emotion Recognition API")
    print("=" * 50)
    print(f"Device: {device}")
    print(f"Model: LateFusionModel (8 emotions)")
    print(f"Upload folder: {app.config['UPLOAD_FOLDER']}")
    print(f"Allowed file types: {app.config['ALLOWED_EXTENSIONS']}")
    print("=" * 50)
    print("Server starting on http://localhost:5000")
    print("Endpoints:")
    print("  GET  /           - API information")
    print("  GET  /health     - Health check")
    print("  GET  /model-info - Model information")
    print("  POST /predict    - Upload video for emotion recognition")
    print("  POST /predict-batch - Upload multiple videos")
    print("=" * 50)
    
    # Run with production settings
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)