import java.awt.*
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder

fun main() {
    SwingUtilities.invokeLater {
        DeliTrackApp().isVisible = true
    }
}

// Persistent File Manager Supporting UNLIMITED Accounts Across System Restarts
object AccountManager {
    private val dataFile = File("user_data.txt")

    // Map storing storedUsername -> storedPassword
    private val accounts = mutableMapOf<String, String>()

    init {
        loadAccountData()
    }

    fun hasAnyAccount(): Boolean {
        return accounts.isNotEmpty()
    }

    fun isValidAccount(user: String, pass: String): Boolean {
        return accounts[user] == pass
    }

    fun accountExists(user: String): Boolean {
        return accounts.containsKey(user)
    }

    fun saveAccountData(user: String, pass: String) {
        accounts[user] = pass
        try {
            // Append the new account to user_data.txt so previous accounts are preserved
            dataFile.appendText("$user|$pass\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadAccountData() {
        try {
            if (dataFile.exists()) {
                dataFile.readLines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.contains("|")) {
                        val parts = trimmed.split("|")
                        if (parts.size >= 2) {
                            accounts[parts[0]] = parts[1]
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class DeliTrackApp : JFrame("DELITRACK System Management") {

    private val cardLayout = CardLayout()
    private val mainPanel = JPanel(cardLayout)
    private val primaryColor = Color(0x0B, 0x4F, 0x2C)

    // Form fields kept at class scope
    private val loginUserField = JTextField()
    private val loginPassField = JPasswordField()

    // Red Notification Bar Component
    private val errorBarLabel = JLabel("", SwingConstants.CENTER).apply {
        font = Font("SansSerif", Font.BOLD, 12)
        foreground = Color.WHITE
        isOpaque = true
        background = Color(0xD9, 0x38, 0x3A)
        border = EmptyBorder(8, 12, 8, 12)
        isVisible = false
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(380, 720)
        isResizable = false
        setLocationRelativeTo(null)

        // Clear input fields on app restart so user must re-enter credentials
        loginUserField.text = ""
        loginPassField.text = ""

        // Register All 11 Screens
        mainPanel.add(createSplashScreen(), "SCREEN_1")
        mainPanel.add(createAdminLoginScreen(), "SCREEN_2")
        mainPanel.add(createCreateAccountScreen(), "SCREEN_3")
        mainPanel.add(createDashboardScreen(), "SCREEN_4")
        mainPanel.add(createNewDeliveryScreen(), "SCREEN_5")
        mainPanel.add(createSelectRouteScreen(), "SCREEN_6")
        mainPanel.add(createDeliveryListScreen(), "SCREEN_7")
        mainPanel.add(createAssignRiderVehicleScreen(), "SCREEN_8")
        mainPanel.add(createPackageDetailsScreen(), "SCREEN_9")
        mainPanel.add(createRouteMapScreen(), "SCREEN_10")
        mainPanel.add(createDeliveryTrackingScreen(), "SCREEN_11")

        add(mainPanel)
        cardLayout.show(mainPanel, "SCREEN_1")
    }

    private fun navigateTo(screenKey: String) {
        errorBarLabel.isVisible = false
        cardLayout.show(mainPanel, screenKey)
    }

    private fun showErrorBar(message: String) {
        errorBarLabel.text = message
        errorBarLabel.isVisible = true
        mainPanel.revalidate()
        mainPanel.repaint()
    }

    // -----------------------------------------------------------------
    // GIS INTERACTIVE STREET MAP COMPONENT
    // -----------------------------------------------------------------
    class InteractiveGisMapPanel(
        private val startLocation: String = "Nasugbu",
        private val endLocation: String = "Lian"
    ) : JPanel() {

        private var zoomLevel = 1.0
        private var offsetX = 0.0
        private var offsetY = 0.0
        private var lastMouseX = 0
        private var lastMouseY = 0

        init {
            background = Color(0xE8, 0xF0, 0xE6)

            val mouseAdapter = object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    lastMouseX = e.x
                    lastMouseY = e.y
                }

                override fun mouseDragged(e: MouseEvent) {
                    val dx = e.x - lastMouseX
                    val dy = e.y - lastMouseY
                    offsetX += dx
                    offsetY += dy
                    lastMouseX = e.x
                    lastMouseY = e.y
                    repaint()
                }

                override fun mouseWheelMoved(e: MouseWheelEvent) {
                    if (e.wheelRotation < 0) zoomIn() else zoomOut()
                }
            }

            addMouseListener(mouseAdapter)
            addMouseMotionListener(mouseAdapter)
            addMouseWheelListener(mouseAdapter)
        }

        fun zoomIn() {
            if (zoomLevel < 3.5) {
                zoomLevel *= 1.25
                repaint()
            }
        }

        fun zoomOut() {
            if (zoomLevel > 0.5) {
                zoomLevel /= 1.25
                repaint()
            }
        }

        fun resetView() {
            zoomLevel = 1.0
            offsetX = 0.0
            offsetY = 0.0
            repaint()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val transform = AffineTransform()
            transform.translate(width / 2.0 + offsetX, height / 2.0 + offsetY)
            transform.scale(zoomLevel, zoomLevel)
            transform.translate(-width / 2.0, -height / 2.0)
            g2.transform = transform

            // Draw Street Grid & Background Map Features
            g2.color = Color(0xD0, 0xDC, 0xCE)
            for (i in -200..600 step 40) {
                g2.drawLine(i, -200, i, 600)
                g2.drawLine(-200, i, 600, i)
            }

            // Draw Secondary Roads
            g2.color = Color.WHITE
            g2.stroke = BasicStroke(6f)
            g2.drawLine(30, 80, 310, 80)
            g2.drawLine(30, 260, 310, 260)
            g2.drawLine(80, 30, 80, 320)
            g2.drawLine(280, 30, 280, 320)

            // Draw Highway Vector Line
            g2.color = Color(0xF7, 0xD0, 0x70)
            g2.stroke = BasicStroke(10f)
            val routePath = Path2D.Double()
            routePath.moveTo(70.0, 60.0)
            routePath.curveTo(120.0, 100.0, 150.0, 180.0, 270.0, 260.0)
            g2.draw(routePath)

            // Draw Active Delivery Route Line
            g2.color = Color(0x1B, 0x43, 0x32)
            g2.stroke = BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.draw(routePath)

            // Draw Location Pins
            drawPin(g2, 70, 60, Color(0x31, 0x82, 0xCE), startLocation)
            drawPin(g2, 270, 260, Color(0xE5, 0x3E, 0x3E), endLocation)
        }

        private fun drawPin(g2: Graphics2D, x: Int, y: Int, color: Color, label: String) {
            g2.color = color
            g2.fillOval(x - 10, y - 10, 20, 20)
            g2.color = Color.WHITE
            g2.fillOval(x - 4, y - 4, 8, 8)

            g2.color = Color(0, 0, 0, 180)
            g2.fillRoundRect(x - 28, y - 30, 56, 16, 6, 6)
            g2.color = Color.WHITE
            g2.font = Font("SansSerif", Font.BOLD, 10)
            g2.drawString(label, x - 22, y - 18)
        }
    }

    // -----------------------------------------------------------------
    // SCREEN 1: SPLASH SCREEN
    // -----------------------------------------------------------------
    private fun createSplashScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = primaryColor
            border = EmptyBorder(40, 24, 30, 24)
        }

        val centerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val logoLabel = JLabel("🚚", SwingConstants.CENTER).apply {
            font = Font("Segoe UI Emoji", Font.PLAIN, 48)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val logoBox = JPanel(GridBagLayout()).apply {
            preferredSize = Dimension(110, 110)
            maximumSize = Dimension(110, 110)
            background = Color(255, 255, 255, 30)
            add(logoLabel)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val titleLabel = JLabel("DELITRACK").apply {
            font = Font("SansSerif", Font.BOLD, 28)
            foreground = Color.WHITE
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val subTitleLabel = JLabel("<html><center>Delivery Route System<br>Management</center></html>").apply {
            font = Font("SansSerif", Font.PLAIN, 13)
            foreground = Color(255, 255, 255, 220)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val routeLabel = JLabel(" Nasugbu  ➔  Lian ").apply {
            font = Font("SansSerif", Font.BOLD, 13)
            foreground = Color.WHITE
            background = Color(255, 255, 255, 40)
            isOpaque = true
            border = EmptyBorder(6, 16, 6, 16)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        centerPanel.add(Box.createVerticalGlue())
        centerPanel.add(logoBox)
        centerPanel.add(Box.createRigidArea(Dimension(0, 20)))
        centerPanel.add(titleLabel)
        centerPanel.add(Box.createRigidArea(Dimension(0, 8)))
        centerPanel.add(subTitleLabel)
        centerPanel.add(Box.createRigidArea(Dimension(0, 24)))
        centerPanel.add(routeLabel)
        centerPanel.add(Box.createVerticalGlue())

        val bottomPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val getStartedBtn = JButton("Get Started").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = primaryColor
            background = Color.WHITE
            isFocusPainted = false
            alignmentX = Component.CENTER_ALIGNMENT
            maximumSize = Dimension(320, 45)
            addActionListener { navigateTo("SCREEN_2") }
        }

        bottomPanel.add(getStartedBtn)

        panel.add(centerPanel, BorderLayout.CENTER)
        panel.add(bottomPanel, BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 2: ADMIN LOGIN
    // -----------------------------------------------------------------
    private fun createAdminLoginScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(16, 24, 20, 24)
        }

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val shieldIcon = JLabel("🛡️", SwingConstants.CENTER).apply {
            font = Font("Segoe UI Emoji", Font.PLAIN, 36)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val brandName = JLabel("DELITRACK").apply {
            font = Font("SansSerif", Font.BOLD, 18)
            foreground = primaryColor
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val pageTitle = JLabel("Admin Login").apply {
            font = Font("SansSerif", Font.PLAIN, 13)
            foreground = Color.GRAY
            alignmentX = Component.CENTER_ALIGNMENT
        }

        contentPanel.add(shieldIcon)
        contentPanel.add(Box.createRigidArea(Dimension(0, 4)))
        contentPanel.add(brandName)
        contentPanel.add(pageTitle)
        contentPanel.add(Box.createRigidArea(Dimension(0, 20)))

        val userLabel = JLabel("Username / Email").apply { font = Font("SansSerif", Font.BOLD, 12) }
        loginUserField.maximumSize = Dimension(320, 38)

        val passLabel = JLabel("Password").apply { font = Font("SansSerif", Font.BOLD, 12) }
        loginPassField.maximumSize = Dimension(320, 38)

        contentPanel.add(alignLeft(userLabel))
        contentPanel.add(Box.createRigidArea(Dimension(0, 4)))
        contentPanel.add(loginUserField)
        contentPanel.add(Box.createRigidArea(Dimension(0, 12)))
        contentPanel.add(alignLeft(passLabel))
        contentPanel.add(Box.createRigidArea(Dimension(0, 4)))
        contentPanel.add(loginPassField)
        contentPanel.add(Box.createRigidArea(Dimension(0, 20)))

        val signInBtn = JButton("Sign In").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(320, 45)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                val inputUser = loginUserField.text.trim()
                val inputPass = String(loginPassField.password)

                if (!AccountManager.hasAnyAccount()) {
                    showErrorBar("No accounts found! Please create an account first.")
                } else if (inputUser.isEmpty()) {
                    showErrorBar("Please enter your username or email.")
                } else if (inputPass.isEmpty()) {
                    showErrorBar("Please re-type your password.")
                } else if (AccountManager.isValidAccount(inputUser, inputPass)) {
                    errorBarLabel.isVisible = false
                    navigateTo("SCREEN_4")
                } else {
                    showErrorBar("Incorrect username or password! Try again.")
                }
            }
        }
        contentPanel.add(signInBtn)

        val footerPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply { isOpaque = false }
        val signUpLink = JLabel("<html><u>Sign up</u></html>").apply {
            font = Font("SansSerif", Font.BOLD, 12)
            foreground = primaryColor
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) { navigateTo("SCREEN_3") }
            })
        }
        footerPanel.add(JLabel("Don't have an account? "))
        footerPanel.add(signUpLink)

        val topHeader = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(errorBarLabel, BorderLayout.NORTH)
        }

        panel.add(topHeader, BorderLayout.NORTH)
        panel.add(contentPanel, BorderLayout.CENTER)
        panel.add(footerPanel, BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 3: CREATE ACCOUNT
    // -----------------------------------------------------------------
    private fun createCreateAccountScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(20, 24, 20, 24)
        }

        val topNav = JPanel(FlowLayout(FlowLayout.LEFT)).apply { isOpaque = false }
        val backBtn = JButton("←").apply {
            font = Font("SansSerif", Font.BOLD, 16)
            isContentAreaFilled = false
            isBorderPainted = false
            addActionListener { navigateTo("SCREEN_2") }
        }
        topNav.add(backBtn)

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val screenTitle = JLabel("Create Account").apply { font = Font("SansSerif", Font.BOLD, 20) }
        val subtitle = JLabel("Join DELITRACK").apply { font = Font("SansSerif", Font.PLAIN, 12); foreground = Color.GRAY }

        contentPanel.add(alignLeft(screenTitle))
        contentPanel.add(alignLeft(subtitle))
        contentPanel.add(Box.createRigidArea(Dimension(0, 16)))

        val nameField = JTextField().apply { maximumSize = Dimension(320, 36) }
        val emailField = JTextField().apply { maximumSize = Dimension(320, 36) }
        val phoneField = JTextField().apply { maximumSize = Dimension(320, 36) }
        val passField = JPasswordField().apply { maximumSize = Dimension(320, 36) }

        contentPanel.add(alignLeft(JLabel("Full Name")))
        contentPanel.add(nameField)
        contentPanel.add(Box.createRigidArea(Dimension(0, 8)))
        contentPanel.add(alignLeft(JLabel("Email Address / Username")))
        contentPanel.add(emailField)
        contentPanel.add(Box.createRigidArea(Dimension(0, 8)))
        contentPanel.add(alignLeft(JLabel("Phone Number")))
        contentPanel.add(phoneField)
        contentPanel.add(Box.createRigidArea(Dimension(0, 8)))
        contentPanel.add(alignLeft(JLabel("Password")))
        contentPanel.add(passField)
        contentPanel.add(Box.createRigidArea(Dimension(0, 20)))

        val createBtn = JButton("Create Account").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(320, 45)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                val newUsername = emailField.text.trim()
                val newPassword = String(passField.password)

                if (newUsername.isEmpty() || newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this@DeliTrackApp,
                        "Please fill in all fields to create an account.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                    )
                } else if (AccountManager.accountExists(newUsername)) {
                    JOptionPane.showMessageDialog(
                        this@DeliTrackApp,
                        "Username '$newUsername' already exists. Please choose a different one or login.",
                        "Account Exists",
                        JOptionPane.WARNING_MESSAGE
                    )
                } else {
                    // Appends and saves permanently to user_data.txt without erasing existing accounts
                    AccountManager.saveAccountData(newUsername, newPassword)

                    // Auto-fill username, but clear password so user re-types it
                    loginUserField.text = newUsername
                    loginPassField.text = ""

                    JOptionPane.showMessageDialog(
                        this@DeliTrackApp,
                        "Account Created Successfully!\nUsername: $newUsername\n\nPlease enter your password to Sign In.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    )

                    navigateTo("SCREEN_2")
                }
            }
        }
        contentPanel.add(createBtn)

        panel.add(topNav, BorderLayout.NORTH)
        panel.add(contentPanel, BorderLayout.CENTER)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 4: DASHBOARD
    // -----------------------------------------------------------------
    private fun createDashboardScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(20, 20, 10, 20)
        }

        val header = JPanel(BorderLayout()).apply { isOpaque = false }
        val welcomeLabel = JLabel("<html><b>Hello, Admin!</b><br><font color='gray'>Good day, keep your deliveries moving.</font></html>")
        header.add(welcomeLabel, BorderLayout.WEST)

        val centerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val actionBox = JPanel(GridLayout(1, 2, 12, 0)).apply {
            maximumSize = Dimension(340, 80)
            isOpaque = false
        }

        val deliveriesBtn = JButton("<html><center>📦<br><b>Deliveries</b></center></html>").apply {
            background = primaryColor
            foreground = Color.WHITE
            isFocusPainted = false
            addActionListener { navigateTo("SCREEN_7") }
        }

        val mapBtn = JButton("<html><center>🗺️<br><b>Route Map</b></center></html>").apply {
            background = primaryColor
            foreground = Color.WHITE
            isFocusPainted = false
            addActionListener { navigateTo("SCREEN_10") }
        }

        actionBox.add(deliveriesBtn)
        actionBox.add(mapBtn)

        val newDeliveryBtn = JButton("+ New Delivery").apply {
            font = Font("SansSerif", Font.BOLD, 13)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(340, 40)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { navigateTo("SCREEN_5") }
        }

        centerPanel.add(Box.createRigidArea(Dimension(0, 16)))
        centerPanel.add(actionBox)
        centerPanel.add(Box.createRigidArea(Dimension(0, 16)))
        centerPanel.add(newDeliveryBtn)

        panel.add(header, BorderLayout.NORTH)
        panel.add(centerPanel, BorderLayout.CENTER)
        panel.add(createBottomNavBar("Dashboard"), BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 5: NEW DELIVERY
    // -----------------------------------------------------------------
    private fun createNewDeliveryScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(16, 20, 10, 20)
        }

        val topNav = JPanel(FlowLayout(FlowLayout.LEFT)).apply { isOpaque = false }
        val backBtn = JButton("← New Delivery").apply {
            font = Font("SansSerif", Font.BOLD, 16)
            isContentAreaFilled = false
            isBorderPainted = false
            addActionListener { navigateTo("SCREEN_4") }
        }
        topNav.add(backBtn)

        val formPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        formPanel.add(alignLeft(JLabel("Pickup Location")))
        formPanel.add(JTextField("Nasugbu, Batangas").apply { maximumSize = Dimension(340, 36) })
        formPanel.add(Box.createRigidArea(Dimension(0, 8)))

        formPanel.add(alignLeft(JLabel("Destination")))
        formPanel.add(JTextField("Lian, Batangas").apply { maximumSize = Dimension(340, 36) })
        formPanel.add(Box.createRigidArea(Dimension(0, 8)))

        formPanel.add(alignLeft(JLabel("Customer")))
        formPanel.add(JTextField("Juan Dela Cruz").apply { maximumSize = Dimension(340, 36) })
        formPanel.add(Box.createRigidArea(Dimension(0, 8)))

        formPanel.add(alignLeft(JLabel("Package / Item")))
        formPanel.add(JComboBox(arrayOf("3 Items", "1 Item", "5 Items")).apply { maximumSize = Dimension(340, 36) })
        formPanel.add(Box.createRigidArea(Dimension(0, 16)))

        val createDeliveryBtn = JButton("Create Delivery").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(340, 42)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { navigateTo("SCREEN_6") }
        }
        formPanel.add(createDeliveryBtn)

        panel.add(topNav, BorderLayout.NORTH)
        panel.add(formPanel, BorderLayout.CENTER)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 6: SELECT ROUTE
    // -----------------------------------------------------------------
    private fun createSelectRouteScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(16, 20, 10, 20)
        }

        val topNav = JPanel(FlowLayout(FlowLayout.LEFT)).apply { isOpaque = false }
        val backBtn = JButton("← Select Route").apply {
            font = Font("SansSerif", Font.BOLD, 16)
            isContentAreaFilled = false
            isBorderPainted = false
            addActionListener { navigateTo("SCREEN_5") }
        }
        topNav.add(backBtn)

        val gisMap = InteractiveGisMapPanel()

        val infoPanel = JPanel(GridLayout(1, 3, 10, 0)).apply { isOpaque = false }
        infoPanel.add(JLabel("<html><center>Distance<br><b>25.6 km</b></center></html>"))
        infoPanel.add(JLabel("<html><center>Est. Time<br><b>45 min</b></center></html>"))
        infoPanel.add(JLabel("<html><center>Traffic<br><font color='green'><b>Light</b></font></center></html>"))

        val bottomPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val useRouteBtn = JButton("Use This Route").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(340, 42)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { navigateTo("SCREEN_7") }
        }

        bottomPanel.add(infoPanel)
        bottomPanel.add(Box.createRigidArea(Dimension(0, 12)))
        bottomPanel.add(useRouteBtn)

        panel.add(topNav, BorderLayout.NORTH)
        panel.add(gisMap, BorderLayout.CENTER)
        panel.add(bottomPanel, BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 7: DELIVERY LIST
    // -----------------------------------------------------------------
    private fun createDeliveryListScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(16, 20, 10, 20)
        }

        val topNav = JPanel(BorderLayout()).apply { isOpaque = false }
        topNav.add(JLabel("Deliveries").apply { font = Font("SansSerif", Font.BOLD, 18) }, BorderLayout.WEST)

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val proceedBtn = JButton("Proceed to Assign Rider & Vehicle").apply {
            font = Font("SansSerif", Font.BOLD, 13)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(340, 42)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { navigateTo("SCREEN_8") }
        }

        contentPanel.add(Box.createRigidArea(Dimension(0, 20)))
        contentPanel.add(proceedBtn)

        panel.add(topNav, BorderLayout.NORTH)
        panel.add(contentPanel, BorderLayout.CENTER)
        panel.add(createBottomNavBar("Deliveries"), BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 8: ASSIGN RIDER & VEHICLE
    // -----------------------------------------------------------------
    private fun createAssignRiderVehicleScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(16, 20, 10, 20)
        }

        val topNav = JPanel(FlowLayout(FlowLayout.LEFT)).apply { isOpaque = false }
        val backBtn = JButton("← Assign Rider & Vehicle").apply {
            font = Font("SansSerif", Font.BOLD, 15)
            isContentAreaFilled = false
            isBorderPainted = false
            addActionListener { navigateTo("SCREEN_7") }
        }
        topNav.add(backBtn)

        val centerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        centerPanel.add(alignLeft(JLabel("Assign Rider").apply { font = Font("SansSerif", Font.BOLD, 12) }))
        centerPanel.add(JRadioButton("Mark Santos (0917 123 4567)", true).apply { isOpaque = false })
        centerPanel.add(JRadioButton("Ana Garcia (0918 765 4321)", false).apply { isOpaque = false })
        centerPanel.add(Box.createRigidArea(Dimension(0, 16)))

        centerPanel.add(alignLeft(JLabel("Assign Vehicle").apply { font = Font("SansSerif", Font.BOLD, 12) }))
        centerPanel.add(JRadioButton("TRK-001 (Closed Van - 1000 kg)", true).apply { isOpaque = false })
        centerPanel.add(JRadioButton("VAN-002 (Closed Van - 800 kg)", false).apply { isOpaque = false })

        val assignBtn = JButton("Assign").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(340, 42)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { navigateTo("SCREEN_9") }
        }

        panel.add(topNav, BorderLayout.NORTH)
        panel.add(centerPanel, BorderLayout.CENTER)
        panel.add(assignBtn, BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 9: PACKAGE DETAILS
    // -----------------------------------------------------------------
    private fun createPackageDetailsScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(16, 20, 10, 20)
        }

        val topNav = JPanel(FlowLayout(FlowLayout.LEFT)).apply { isOpaque = false }
        val backBtn = JButton("← Package / Item Details").apply {
            font = Font("SansSerif", Font.BOLD, 15)
            isContentAreaFilled = false
            isBorderPainted = false
            addActionListener { navigateTo("SCREEN_8") }
        }
        topNav.add(backBtn)

        val listPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        listPanel.add(createItemCard("Electronics", "2 pcs", "5.0 kg"))
        listPanel.add(createItemCard("Office Supplies", "3 pcs", "3.5 kg"))
        listPanel.add(createItemCard("Documents", "1 pc", "0.5 kg"))

        val saveBtn = JButton("Save").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(340, 42)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { navigateTo("SCREEN_10") }
        }

        panel.add(topNav, BorderLayout.NORTH)
        panel.add(listPanel, BorderLayout.CENTER)
        panel.add(saveBtn, BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 10: ROUTE MAP
    // -----------------------------------------------------------------
    private fun createRouteMapScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(16, 20, 10, 20)
        }

        val topNav = JPanel(FlowLayout(FlowLayout.LEFT)).apply { isOpaque = false }
        val backBtn = JButton("← Route Map DLV-1001").apply {
            font = Font("SansSerif", Font.BOLD, 15)
            isContentAreaFilled = false
            isBorderPainted = false
            addActionListener { navigateTo("SCREEN_9") }
        }
        topNav.add(backBtn)

        val gisMapPanel = InteractiveGisMapPanel("Nasugbu", "Lian")
        val mapContainer = JPanel(BorderLayout())
        mapContainer.add(gisMapPanel, BorderLayout.CENTER)

        val zoomControlPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            isOpaque = false
            val zoomInBtn = JButton("+").apply {
                font = Font("SansSerif", Font.BOLD, 16)
                isFocusPainted = false
                addActionListener { gisMapPanel.zoomIn() }
            }
            val zoomOutBtn = JButton("-").apply {
                font = Font("SansSerif", Font.BOLD, 16)
                isFocusPainted = false
                addActionListener { gisMapPanel.zoomOut() }
            }
            val resetBtn = JButton("⟲").apply {
                font = Font("SansSerif", Font.BOLD, 14)
                isFocusPainted = false
                addActionListener { gisMapPanel.resetView() }
            }
            add(zoomInBtn)
            add(zoomOutBtn)
            add(resetBtn)
        }
        mapContainer.add(zoomControlPanel, BorderLayout.SOUTH)

        val infoBar = JPanel(GridLayout(1, 2)).apply {
            isOpaque = false
            add(JLabel("Distance: 25.6 km"))
            add(JLabel("Est. Time: 45 min"))
        }

        val trackBtn = JButton("View Live Tracking").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(340, 42)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { navigateTo("SCREEN_11") }
        }

        val bottomBox = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            add(infoBar)
            add(Box.createRigidArea(Dimension(0, 10)))
            add(trackBtn)
        }

        panel.add(topNav, BorderLayout.NORTH)
        panel.add(mapContainer, BorderLayout.CENTER)
        panel.add(bottomBox, BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // SCREEN 11: DELIVERY TRACKING
    // -----------------------------------------------------------------
    private fun createDeliveryTrackingScreen(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Color(245, 247, 246)
            border = EmptyBorder(16, 20, 10, 20)
        }

        val topNav = JPanel(FlowLayout(FlowLayout.LEFT)).apply { isOpaque = false }
        val backBtn = JButton("← Delivery Tracking").apply {
            font = Font("SansSerif", Font.BOLD, 15)
            isContentAreaFilled = false
            isBorderPainted = false
            addActionListener { navigateTo("SCREEN_10") }
        }
        topNav.add(backBtn)

        val contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        contentPanel.add(alignLeft(JLabel("<html><b>Status:</b> <font color='#3182CE'>In Transit</font></html>")))
        contentPanel.add(Box.createRigidArea(Dimension(0, 10)))
        contentPanel.add(alignLeft(JLabel("<b>Pickup:</b> Nasugbu, Batangas")))
        contentPanel.add(alignLeft(JLabel("<b>Destination:</b> Lian, Batangas")))
        contentPanel.add(alignLeft(JLabel("<b>Customer:</b> Juan Dela Cruz")))
        contentPanel.add(Box.createRigidArea(Dimension(0, 12)))

        val liveGisMap = InteractiveGisMapPanel("Nasugbu", "Lian")
        liveGisMap.preferredSize = Dimension(340, 150)
        contentPanel.add(liveGisMap)
        contentPanel.add(Box.createRigidArea(Dimension(0, 12)))

        contentPanel.add(alignLeft(JLabel("<b>Rider:</b> Mark Santos (0917 123 4567)")))
        contentPanel.add(alignLeft(JLabel("<b>Vehicle:</b> TRK-001 (Closed Van)")))
        contentPanel.add(Box.createRigidArea(Dimension(0, 10)))
        contentPanel.add(alignLeft(JLabel("<html><b>ETA:</b> 09:30 AM (45 min remaining)</html>")))

        val restartBtn = JButton("Return to Dashboard").apply {
            font = Font("SansSerif", Font.BOLD, 14)
            foreground = Color.WHITE
            background = primaryColor
            isFocusPainted = false
            maximumSize = Dimension(340, 42)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener { navigateTo("SCREEN_4") }
        }

        panel.add(topNav, BorderLayout.NORTH)
        panel.add(contentPanel, BorderLayout.CENTER)
        panel.add(restartBtn, BorderLayout.SOUTH)

        return panel
    }

    // -----------------------------------------------------------------
    // UI HELPER METHODS
    // -----------------------------------------------------------------
    private fun createItemCard(name: String, pcs: String, weight: String): JPanel {
        return JPanel(BorderLayout()).apply {
            background = Color.WHITE
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color(220, 220, 220)),
                EmptyBorder(10, 12, 10, 12)
            )
            maximumSize = Dimension(340, 50)
            add(JLabel("📦 <b>$name</b>"), BorderLayout.WEST)
            add(JLabel("$pcs | $weight"), BorderLayout.EAST)
        }
    }

    private fun createBottomNavBar(activeTab: String): JPanel {
        val bar = JPanel(GridLayout(1, 4)).apply {
            background = Color.WHITE
            preferredSize = Dimension(340, 45)
            border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY)
        }

        val tabs = listOf("Home", "Deliveries", "Map", "More")
        for (tab in tabs) {
            val btn = JButton(tab).apply {
                isContentAreaFilled = false
                isBorderPainted = false
                font = Font("SansSerif", if (tab == activeTab) Font.BOLD else Font.PLAIN, 10)
                foreground = if (tab == activeTab) primaryColor else Color.GRAY
                addActionListener {
                    when (tab) {
                        "Home" -> navigateTo("SCREEN_4")
                        "Deliveries" -> navigateTo("SCREEN_7")
                        "Map" -> navigateTo("SCREEN_10")
                    }
                }
            }
            bar.add(btn)
        }
        return bar
    }

    private fun alignLeft(component: Component): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            maximumSize = Dimension(340, 24)
            add(component)
        }
    }
}