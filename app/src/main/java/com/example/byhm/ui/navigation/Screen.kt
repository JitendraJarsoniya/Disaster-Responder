package com.example.byhm.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Map : Screen("map")
    object ResourceDirectory : Screen("resource_directory")
    object ResourceDetail : Screen("resource_detail/{resourceId}") {
        fun createRoute(resourceId: String) = "resource_detail/$resourceId"
    }
    object IncidentReport : Screen("incident_report")
    object MyReports : Screen("my_reports")
    object Alerts : Screen("alerts")
    object AlertDetail : Screen("alert_detail/{alertId}") {
        fun createRoute(alertId: String) = "alert_detail/$alertId"
    }
    object AiAssistant : Screen("ai_assistant")
    object ResponderDashboard : Screen("responder_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object Settings : Screen("settings")
    object More : Screen("more")
    object EmergencyGuide : Screen("emergency_guide")
    object HelpInfo : Screen("help_info")
    object EmergencyProfile : Screen("emergency_profile")
}
