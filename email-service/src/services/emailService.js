const nodemailer = require('nodemailer');
const { v4: uuidv4 } = require('uuid');
const EmailLog = require('../models/EmailLog');
const { getVerificationTemplate } = require('../templates/verificationTemplate');
const { getReportTemplate } = require('../templates/reportTemplate');

class EmailService {
  constructor() {
    this.transporter = null;
    this.initialize();
  }

  initialize() {
    // Create nodemailer transporter
    this.transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST || 'smtp.gmail.com',
      port: parseInt(process.env.SMTP_PORT) || 587,
      secure: process.env.SMTP_SECURE === 'true', // true for 465, false for other ports
      auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASS
      },
      tls: {
        rejectUnauthorized: false
      }
    });

    console.log('✅ Email transporter initialized');
  }

  async sendVerificationEmail(to, username, verificationCode, verificationLink) {
    const emailId = uuidv4();
    const trackingPixelUrl = `${process.env.SERVICE_URL || 'http://localhost:3000'}/track/${emailId}`;

    try {
      const htmlContent = getVerificationTemplate(
        username,
        verificationCode,
        verificationLink,
        trackingPixelUrl
      );

      // Create email log entry
      const emailLog = new EmailLog({
        emailId,
        to,
        from: process.env.SMTP_FROM || process.env.SMTP_USER,
        subject: 'Verify Your Email Address',
        type: 'verification',
        metadata: {
          username,
          verificationCode
        }
      });

      // Send email
      const info = await this.transporter.sendMail({
        from: `"${process.env.SMTP_FROM_NAME || 'Your Company'}" <${process.env.SMTP_FROM || process.env.SMTP_USER}>`,
        to,
        subject: 'Verify Your Email Address',
        html: htmlContent
      });

      // Update log with sent info
      emailLog.sent = true;
      emailLog.sentAt = new Date();
      await emailLog.save();

      console.log(`Verification email sent to ${to} - ID: ${emailId}`);

      return {
        success: true,
        message: 'Verification email sent successfully',
        emailId,
        messageId: info.messageId
      };

    } catch (error) {
      console.error('Error sending verification email:', error);

      // Save error to database
      try {
        await EmailLog.create({
          emailId,
          to,
          from: process.env.SMTP_FROM || process.env.SMTP_USER,
          subject: 'Verify Your Email Address',
          type: 'verification',
          sent: false,
          error: error.message,
          metadata: {
            username,
            verificationCode
          }
        });
      } catch (dbError) {
        console.error('Error saving email log:', dbError);
      }

      return {
        success: false,
        message: `Failed to send verification email: ${error.message}`,
        emailId
      };
    }
  }

  async sendReportEmail(to, username, reportTitle, reportContent, reportDate, reportData) {
    const emailId = uuidv4();
    const trackingPixelUrl = `${process.env.SERVICE_URL || 'http://localhost:3000'}/track/${emailId}`;

    try {
      const htmlContent = getReportTemplate(
        username,
        reportTitle,
        reportContent,
        reportDate,
        reportData,
        trackingPixelUrl
      );

      // Create email log entry
      const emailLog = new EmailLog({
        emailId,
        to,
        from: process.env.SMTP_FROM || process.env.SMTP_USER,
        subject: `Report: ${reportTitle}`,
        type: 'report',
        metadata: {
          username,
          reportTitle,
          reportDate
        }
      });

      // Send email
      const info = await this.transporter.sendMail({
        from: `"${process.env.SMTP_FROM_NAME || 'Your Company'}" <${process.env.SMTP_FROM || process.env.SMTP_USER}>`,
        to,
        subject: `Report: ${reportTitle}`,
        html: htmlContent
      });

      // Update log with sent info
      emailLog.sent = true;
      emailLog.sentAt = new Date();
      await emailLog.save();

      console.log(`Report email sent to ${to} - ID: ${emailId}`);

      return {
        success: true,
        message: 'Report email sent successfully',
        emailId,
        messageId: info.messageId
      };

    } catch (error) {
      console.error('Error sending report email:', error);

      // Save error to database
      try {
        await EmailLog.create({
          emailId,
          to,
          from: process.env.SMTP_FROM || process.env.SMTP_USER,
          subject: `Report: ${reportTitle}`,
          type: 'report',
          sent: false,
          error: error.message,
          metadata: {
            username,
            reportTitle,
            reportDate
          }
        });
      } catch (dbError) {
        console.error('Error saving email log:', dbError);
      }

      return {
        success: false,
        message: `Failed to send report email: ${error.message}`,
        emailId
      };
    }
  }

  async getEmailStatus(emailId) {
    try {
      const emailLog = await EmailLog.findOne({ emailId });

      if (!emailLog) {
        return {
          found: false,
          message: 'Email not found'
        };
      }

      return {
        found: true,
        sent: emailLog.sent,
        opened: emailLog.opened,
        openedAt: emailLog.openedAt ? emailLog.openedAt.toISOString() : null,
        openCount: emailLog.openCount
      };

    } catch (error) {
      console.error('Error getting email status:', error);
      return {
        found: false,
        message: `Error: ${error.message}`
      };
    }
  }

  async trackEmailOpen(emailId) {
    try {
      const emailLog = await EmailLog.findOne({ emailId });

      if (emailLog) {
        if (!emailLog.opened) {
          emailLog.opened = true;
          emailLog.openedAt = new Date();
        }
        
        emailLog.openCount += 1;
        emailLog.lastOpenedAt = new Date();
        await emailLog.save();

        console.log(`Email opened: ${emailId} - Count: ${emailLog.openCount}`);
      }

    } catch (error) {
      console.error('Error tracking email open:', error);
    }
  }
}

module.exports = EmailService;
