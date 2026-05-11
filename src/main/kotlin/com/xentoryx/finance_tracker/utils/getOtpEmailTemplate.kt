package com.xentoryx.finance_tracker.utils

fun getOtpEmailTemplate(fullName: String, otp: String): String = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Verify Your Email</title>
</head>
<body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
    <tr><td align="center">
      <table width="600" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
        <tr>
          <td style="background:linear-gradient(135deg,#667eea,#764ba2);padding:40px;text-align:center;">
            <h1 style="margin:0;color:#fff;font-size:26px;">💰 OptiSpend</h1>
            <p style="margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:14px;">Smart Personal Finance Tracker</p>
          </td>
        </tr>
        <tr>
          <td style="padding:48px;">
            <h2 style="color:#1a202c;">Verify Your Email Address</h2>
            <p style="color:#4a5568;">Hi <strong>${fullName}</strong>, welcome to OptiSpend! 🎉<br/>Use the code below to verify your account.</p>
            <div style="text-align:center;margin:32px 0;">
              <div style="background:#eef2ff;border:2px dashed #667eea;border-radius:12px;padding:28px 40px;display:inline-block;">
                <p style="margin:0 0 8px;color:#667eea;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Verification Code</p>
                <p style="margin:0;color:#2d3748;font-size:44px;font-weight:800;letter-spacing:14px;font-family:monospace;">${otp}</p>
              </div>
            </div>
            <p style="color:#744210;background:#fff8f0;border-left:4px solid #ed8936;padding:16px;border-radius:0 8px 8px 0;">
              ⏰ This code expires in <strong>10 minutes</strong>. Do not share it with anyone.
            </p>
            <p style="color:#718096;font-size:14px;margin-top:24px;">Didn't create an account? You can safely ignore this email.</p>
          </td>
        </tr>
        <tr>
          <td style="padding:24px;text-align:center;border-top:1px solid #e2e8f0;">
            <p style="margin:0;color:#a0aec0;font-size:13px;">© 2025 OptiSpend · All rights reserved</p>
          </td>
        </tr>
      </table>
    </td></tr>
  </table>
</body>
</html>
""".trimIndent()
