Woo App Config Manager

Install:
1) WordPress Admin -> Plugins -> Add New -> Upload Plugin
2) Upload woo-app-config-manager.zip
3) Activate plugin
4) Go to Settings -> Permalinks and click Save once
5) Open App Config menu in admin

Tabs:
- JSON Editor
- GUI Builder (collapsible sections + rows)

GUI supports:
- search_tab repeatable rows with type/view/link controls
- story/banner editors with media picker
- category images editor
- review criteria editor
- free shipping method dropdown

Endpoints:
- /app/config-test.php
- /wp-json/woo-app-config/v1/config

Note:
- Existing /app/config.php remains untouched.
