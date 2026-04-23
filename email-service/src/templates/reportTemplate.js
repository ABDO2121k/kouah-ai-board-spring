function getReportTemplate(
  username,
  reportTitle,
  reportContent,
  reportDate,
  reportData,
  trackingPixelUrl,
) {
  const reportDataRows =
    reportData && reportData.length > 0
      ? reportData
          .map(
            (item) => `
        <tr>
          <td style="padding: 12px; border-bottom: 1px solid #e0e0e0; font-weight: 600; color: #555;">${item.key}</td>
          <td style="padding: 12px; border-bottom: 1px solid #e0e0e0; color: #333;">${item.value}</td>
        </tr>
      `,
          )
          .join("")
      : '<tr><td colspan="2" style="padding: 12px; text-align: center; color: #999;">No data available</td></tr>';

  return `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Report - ${reportTitle}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f0f2f5;
            padding: 20px;
        }
        
        .email-container {
            max-width: 700px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        }
        
        .header {
            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #7e8ba3 100%);
            padding: 40px 30px;
            color: #ffffff;
        }
        
        .header-content {
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        
        .header h1 {
            font-size: 26px;
            font-weight: 600;
            margin-bottom: 8px;
        }
        
        .header .date {
            font-size: 14px;
            opacity: 0.9;
            background-color: rgba(255, 255, 255, 0.2);
            padding: 6px 12px;
            border-radius: 20px;
            display: inline-block;
        }
        
        .report-badge {
            background-color: #4CAF50;
            color: white;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 1px;
            display: inline-block;
            margin-top: 10px;
        }
        
        .content {
            padding: 40px 30px;
        }
        
        .greeting {
            font-size: 18px;
            color: #333333;
            margin-bottom: 20px;
        }
        
        .report-title {
            font-size: 22px;
            color: #1e3c72;
            font-weight: 600;
            margin: 25px 0 15px;
            padding-bottom: 10px;
            border-bottom: 3px solid #2a5298;
        }
        
        .report-description {
            font-size: 15px;
            color: #666666;
            line-height: 1.7;
            margin-bottom: 30px;
            padding: 20px;
            background-color: #f8f9fa;
            border-left: 4px solid #2a5298;
            border-radius: 4px;
        }
        
        .data-table-container {
            margin: 30px 0;
            overflow-x: auto;
        }
        
        .data-table {
            width: 100%;
            border-collapse: collapse;
            background-color: #ffffff;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            border-radius: 8px;
            overflow: hidden;
        }
        
        .data-table thead {
            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
            color: #ffffff;
        }
        
        .data-table th {
            padding: 15px 12px;
            text-align: left;
            font-weight: 600;
            font-size: 14px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .data-table td {
            padding: 12px;
            border-bottom: 1px solid #e0e0e0;
            color: #333;
        }
        
        .data-table tbody tr:hover {
            background-color: #f5f7fa;
        }
        
        .data-table tbody tr:last-child td {
            border-bottom: none;
        }
        
        .summary-box {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            border-radius: 8px;
            padding: 25px;
            margin: 30px 0;
            border-left: 5px solid #2196F3;
        }
        
        .summary-title {
            font-size: 16px;
            font-weight: 600;
            color: #1565c0;
            margin-bottom: 12px;
        }
        
        .summary-text {
            font-size: 14px;
            color: #424242;
            line-height: 1.6;
        }
        
        .action-box {
            background-color: #fff3e0;
            border-radius: 8px;
            padding: 20px;
            margin: 25px 0;
            text-align: center;
            border: 2px dashed #ff9800;
        }
        
        .action-title {
            font-size: 15px;
            color: #e65100;
            font-weight: 600;
            margin-bottom: 10px;
        }
        
        .action-button {
            display: inline-block;
            padding: 12px 30px;
            background: linear-gradient(135deg, #ff6f00 0%, #ff9800 100%);
            color: #ffffff;
            text-decoration: none;
            border-radius: 25px;
            font-size: 14px;
            font-weight: 600;
            margin-top: 10px;
            transition: transform 0.2s, box-shadow 0.2s;
            box-shadow: 0 3px 10px rgba(255, 111, 0, 0.3);
        }
        
        .action-button:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(255, 111, 0, 0.5);
        }
        
        .footer {
            background-color: #263238;
            padding: 30px;
            color: #b0bec5;
            font-size: 13px;
            line-height: 1.6;
        }
        
        .footer-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }
        
        .footer-section h3 {
            color: #ffffff;
            font-size: 14px;
            margin-bottom: 10px;
        }
        
        .footer-section p {
            font-size: 12px;
            line-height: 1.5;
        }
        
        .footer-bottom {
            text-align: center;
            padding-top: 20px;
            border-top: 1px solid #37474f;
            color: #78909c;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 15px;
            margin: 25px 0;
        }
        
        .stat-card {
            background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            border-left: 4px solid #2a5298;
        }
        
        .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #1e3c72;
            margin-bottom: 5px;
        }
        
        .stat-label {
            font-size: 12px;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="header">
            <h1>📊 Report Generated</h1>
            <div class="date">📅 ${reportDate}</div>
            <div class="report-badge">Automated Report</div>
        </div>
        
        <div class="content">
            <div class="greeting">
                Hello <strong>${username}</strong>,
            </div>
            
            <div class="report-title">
                ${reportTitle}
            </div>
            
            <div class="report-description">
                ${reportContent}
            </div>
            
            <div class="data-table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Metric</th>
                            <th>Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${reportDataRows}
                    </tbody>
                </table>
            </div>
            
            <div class="summary-box">
                <div class="summary-title">📌 Summary</div>
                <div class="summary-text">
                    This report has been automatically generated and contains the latest data as of ${reportDate}. 
                    Please review the information carefully and take any necessary actions.
                </div>
            </div>
            
            <div class="action-box">
                <div class="action-title">Need to take action?</div>
                <a href="#" class="action-button">View Full Report Dashboard</a>
            </div>
        </div>
        
        <div class="footer">
            <div class="footer-grid">
                <div class="footer-section">
                    <h3>About This Report</h3>
                    <p>This is an automated report generated by our system. The data is updated in real-time and reflects the most current information available.</p>
                </div>
                <div class="footer-section">
                    <h3>Need Help?</h3>
                    <p>If you have questions about this report or need assistance, please contact our support team at support@yourcompany.com</p>
                </div>
            </div>
            <div class="footer-bottom">
                <div>© 2026 Your Company. All rights reserved.</div>
                <div style="margin-top: 10px; font-size: 11px;">
                    This email was sent to ${username}. You're receiving this because you're subscribed to automated reports.
                </div>
            </div>
        </div>
    </div>
    
    <!-- Tracking Pixel -->
    <img src="${trackingPixelUrl}" width="1" height="1" style="display:none;" alt="" />
</body>
</html>
  `;
}

module.exports = { getReportTemplate };
