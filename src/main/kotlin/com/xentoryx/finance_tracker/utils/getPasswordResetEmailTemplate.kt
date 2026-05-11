package com.xentoryx.finance_tracker.utils

fun getPasswordResetEmailTemplate(fullName: String, resetToken: String): String = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <title>Reset Your Password</title>
</head>
<body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
    <tr><td align="center">
      <table width="600" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
        <tr>
          <td style="background:linear-gradient(135deg,#f093fb,#f5576c);padding:40px;text-align:center;">
            <h1 style="margin:0;color:#fff;font-size:26px;">🔐 OptiSpend</h1>
            <p style="margin:6px 0 0;color:rgba(255,255,255,0.8);font-size:14px;">Password Reset Request</p>
          </td>
        </tr>
        <tr>
          <td style="padding:48px;">
            <h2 style="color:#1a202c;">Reset Your Password</h2>
            <p style="color:#4a5568;">Hi <strong>${fullName}</strong>,<br/>Use the token below to reset your password.</p>
            <div style="text-align:center;margin:32px 0;">
              <div style="background:#fff5f5;border:2px dashed #f5576c;border-radius:12px;padding:24px 32px;">
                <p style="margin:0 0 10px;color:#f5576c;font-size:11px;letter-spacing:2px;text-transform:uppercase;">Reset Token</p>
                <p style="margin:0;color:#2d3748;font-size:12px;font-weight:700;font-family:monospace;word-break:break-all;background:#f7fafc;padding:12px;border-radius:8px;">${resetToken}</p>
              </div>
            </div>
            <p style="color:#744210;background:#fff8f0;border-left:4px solid #ed8936;padding:16px;border-radius:0 8px 8px 0;">
              ⏰ This token expires in <strong>1 hour</strong>. Never share it with anyone.
            </p>
            <p style="color:#718096;font-size:14px;margin-top:24px;">Didn't request this? Please secure your account immediately.</p>
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
