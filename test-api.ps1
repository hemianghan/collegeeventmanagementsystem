# Test the notification API
Write-Host "Testing Admin Notification Sending..." -ForegroundColor Green

$notificationData = @{
    title = "Test Admin Notification"
    message = "This is a test notification from admin to all students"
    type = "announcement"
    audience = "ALL_STUDENTS"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications" -Method POST -Body $notificationData -ContentType "application/json"
    Write-Host "✅ Notification sent successfully!" -ForegroundColor Green
    Write-Host "Recipients: $($response.recipientCount)" -ForegroundColor Yellow
    Write-Host "Message: $($response.message)" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Error sending notification: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTesting Student Notification Retrieval..." -ForegroundColor Green

# Test student 1001 notifications
try {
    $studentNotifications = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications/user/1001" -Method GET
    Write-Host "✅ Student 1001 has $($studentNotifications.Count) notifications" -ForegroundColor Green
    if ($studentNotifications.Count -gt 0) {
        Write-Host "Latest: $($studentNotifications[0].title)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Error getting student notifications: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTesting Student Past Events..." -ForegroundColor Green

# Test student 1001 registrations
try {
    $studentRegistrations = Invoke-RestMethod -Uri "http://localhost:8080/api/registrations/user/1001/detailed" -Method GET
    Write-Host "✅ Student 1001 has $($studentRegistrations.Count) registrations" -ForegroundColor Green
    
    $today = Get-Date
    $pastEvents = $studentRegistrations | Where-Object { [DateTime]$_.event.date -lt $today }
    $futureEvents = $studentRegistrations | Where-Object { [DateTime]$_.event.date -ge $today }
    
    Write-Host "Past events: $($pastEvents.Count)" -ForegroundColor Yellow
    Write-Host "Future events: $($futureEvents.Count)" -ForegroundColor Yellow
    
    if ($pastEvents.Count -gt 0) {
        Write-Host "Sample past event: $($pastEvents[0].event.title)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Error getting student registrations: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Test completed!" -ForegroundColor Green