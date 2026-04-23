function getVerificationTemplate(
  username,
  verificationCode,
  verificationLink,
  trackingPixelUrl,
) {
  const serviceUrl = process.env.SERVICE_URL || "http://localhost:3000";
  const testImageUrl = `${serviceUrl}/images/image.png`;

  return `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Email Verification</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f7fa;
            padding: 20px;
        }
        
        .email-container {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 40px 30px;
            text-align: center;
            color: #ffffff;
        }
        
        .header h1 {
            font-size: 28px;
            font-weight: 600;
            margin-bottom: 10px;
        }
        
        .header p {
            font-size: 16px;
            opacity: 0.9;
        }
        
        .content {
            padding: 40px 30px;
        }
        
        .greeting {
            font-size: 18px;
            color: #333333;
            margin-bottom: 20px;
        }
        
        .message {
            font-size: 15px;
            color: #666666;
            line-height: 1.6;
            margin-bottom: 30px;
        }
        
        .verification-box {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            border-radius: 8px;
            padding: 25px;
            text-align: center;
            margin: 30px 0;
        }
        
        .verification-code {
            font-size: 32px;
            font-weight: bold;
            color: #667eea;
            letter-spacing: 8px;
            margin: 15px 0;
            font-family: 'Courier New', monospace;
        }
        
        .code-label {
            font-size: 14px;
            color: #666666;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .button-container {
            text-align: center;
            margin: 30px 0;
        }
        
        .verify-button {
            display: inline-block;
            padding: 15px 40px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #ffffff;
            text-decoration: none;
            border-radius: 50px;
            font-size: 16px;
            font-weight: 600;
            transition: transform 0.2s, box-shadow 0.2s;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
        }
        
        .verify-button:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
        }
        
        .divider {
            text-align: center;
            margin: 25px 0;
            color: #999999;
            font-size: 14px;
        }
        
        .link-section {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
        }
        
        .link-label {
            font-size: 13px;
            color: #666666;
            margin-bottom: 8px;
        }
        
        .link-url {
            font-size: 13px;
            color: #667eea;
            word-break: break-all;
            font-family: 'Courier New', monospace;
        }
        
        .footer {
            background-color: #f8f9fa;
            padding: 30px;
            text-align: center;
            color: #999999;
            font-size: 13px;
            line-height: 1.6;
        }
        
        .footer-warning {
            margin-top: 15px;
            padding: 15px;
            background-color: #fff3cd;
            border-left: 4px solid #ffc107;
            color: #856404;
            text-align: left;
            border-radius: 4px;
        }
        
        .security-note {
            margin-top: 20px;
            font-size: 12px;
            color: #666666;
        }
        
        .test-image {
            width: 100%;
            max-width: 540px;
            height: auto;
            margin: 20px 0;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        .image-container {
            text-align: center;
            padding: 20px 0;
            background-color: #f8f9fa;
            border-radius: 8px;
            margin: 20px 0;
        }
        
        .image-label {
            font-size: 12px;
            color: #999;
            margin-top: 10px;
        }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="header">
            <h1>🔐 Verify Your Email</h1>
            <p>Secure your account in one simple step</p>
        </div>
        
        <div class="content">
            <div class="greeting">
                Hello <strong>${username}</strong>,
            </div>
            
            <!-- Test Image from ngrok -->
            <div class="image-container">
                <img src="${testImageUrl}" alt="Test Image" class="test-image" />
                <div class="image-label">Test Image - Loaded from: ${serviceUrl}</div>
            </div>
            
            <div class="message">
                Thank you for registering with us! To complete your registration and secure your account, 
                please verify your email address by using the verification code below or clicking the button.
            </div>
            
            <div class="verification-box">
                <div class="code-label">Your Verification Code</div>
                <div class="verification-code">${verificationCode}</div>
                <div class="code-label">Valid for 15 minutes</div>
            </div>
            
            <div class="button-container">
                <a href="${verificationLink}" class="verify-button">Verify Email Address</a>
            </div>
            
            <div class="divider">
                — OR —
            </div>
            
            <div class="link-section">
                <div class="link-label">If the button doesn't work, copy and paste this link into your browser:</div>
                <div class="link-url">${verificationLink}</div>
            </div>
            
            <div class="security-note">
                <strong>Security Note:</strong> This code will expire in 15 minutes. If you didn't request this verification, 
                please ignore this email or contact our support team.
            </div>
        </div>
        
        <div class="footer">
            <div>
                This email was sent to verify your account registration.
            </div>
            <div class="footer-warning">
                <strong>⚠️ Important:</strong> Never share your verification code with anyone. 
                Our team will never ask for this code.
            </div>
            <div style="margin-top: 20px;">
                © 2026 Your Company. All rights reserved.
            </div>
        </div>
    </div>
    
    <!-- Tracking Pixel -->
    <img src="${trackingPixelUrl}" width="1" height="1" style="display:none;" alt="" />
</body>
</html>
  `;
}

module.exports = { getVerificationTemplate };
