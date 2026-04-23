require('dotenv').config();
const express = require('express');
const path = require('path');
const fs = require('fs');
const { connectDatabase } = require('./config/database');
const GrpcServer = require('./grpc/grpcServer');
const EmailService = require('./services/emailService');

const app = express();
const HTTP_PORT = process.env.HTTP_PORT || 3000;
const GRPC_PORT = process.env.GRPC_PORT || 50051;

// Initialize email service for tracking
const emailService = new EmailService();

// Middleware
app.use(express.json());
app.use(express.static(path.join(__dirname, '../public')));

// Tracking pixel endpoint
app.get('/track/:emailId', async (req, res) => {
  const { emailId } = req.params;
  
  console.log(`Tracking pixel requested for email: ${emailId}`);
  
  // Track the email open asynchronously
  emailService.trackEmailOpen(emailId).catch(error => {
    console.error('Error tracking email:', error);
  });

  // Send 1x1 transparent pixel
  const pixelPath = path.join(__dirname, '../public/images/pixel.png');
  
  if (fs.existsSync(pixelPath)) {
    res.setHeader('Content-Type', 'image/png');
    res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, private');
    res.setHeader('Pragma', 'no-cache');
    res.sendFile(pixelPath);
  } else {
    // If pixel file doesn't exist, send a base64 encoded 1x1 transparent PNG
    const pixel = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==', 'base64');
    res.setHeader('Content-Type', 'image/png');
    res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, private');
    res.setHeader('Pragma', 'no-cache');
    res.send(pixel);
  }
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    service: 'email-service',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// API endpoint to check email status (for debugging)
app.get('/api/email-status/:emailId', async (req, res) => {
  const { emailId } = req.params;
  
  try {
    const status = await emailService.getEmailStatus(emailId);
    res.json(status);
  } catch (error) {
    res.status(500).json({
      error: 'Failed to get email status',
      message: error.message
    });
  }
});

// Main startup function
async function startServer() {
  try {
    console.log('Starting Email Service...');
    
    // Connect to MongoDB
    await connectDatabase();
    
    // Start HTTP server for tracking
    app.listen(HTTP_PORT, '0.0.0.0', () => {
      console.log(`HTTP Server running on port ${HTTP_PORT}`);
      console.log(`Tracking endpoint: http://localhost:${HTTP_PORT}/track/:emailId`);
    });
    
    // Start gRPC server
    const grpcServer = new GrpcServer();
    await grpcServer.start(GRPC_PORT);
    
    console.log('Email Service is ready!');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log(`   HTTP Port: ${HTTP_PORT}`);
    console.log(`   gRPC Port: ${GRPC_PORT}`);
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    
    // Graceful shutdown
    process.on('SIGINT', async () => {
      console.log('\nShutting down gracefully...');
      await grpcServer.stop();
      await mongoose.connection.close();
      process.exit(0);
    });
    
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
}

// Start the server
startServer();
