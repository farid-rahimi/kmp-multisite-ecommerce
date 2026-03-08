<?php
/**
 * Plugin Name: Woo App Config Manager
 * Description: Manage mobile app configuration from WordPress admin, cache JSON to a file, and expose a test endpoint without touching /app/config.php.
 * Version: 1.1.0
 * Author: Solutionium
 */

if (!defined('ABSPATH')) {
    exit;
}

final class Woo_App_Config_Manager {
    const OPTION_KEY = 'woo_app_config_json';
    const QUERY_VAR = 'woo_app_config_test';
    const REST_NS = 'woo-app-config/v1';
    const FAST_DIR_RELATIVE = 'app';
    const FAST_JSON_FILE = 'config-test.json';
    const FAST_PHP_FILE = 'config-test.php';

    public static function init() {
        add_action('admin_menu', [__CLASS__, 'register_admin_menu']);
        add_action('admin_init', [__CLASS__, 'register_setting']);
        add_action('admin_enqueue_scripts', [__CLASS__, 'enqueue_admin_assets']);

        add_action('update_option_' . self::OPTION_KEY, [__CLASS__, 'handle_option_updated'], 10, 3);

        add_action('rest_api_init', [__CLASS__, 'register_rest_routes']);

        add_filter('query_vars', [__CLASS__, 'register_query_vars']);
        add_action('init', [__CLASS__, 'register_rewrite_rules']);
        add_action('template_redirect', [__CLASS__, 'handle_test_endpoint']);

        register_activation_hook(__FILE__, [__CLASS__, 'activate']);
        register_deactivation_hook(__FILE__, [__CLASS__, 'deactivate']);
    }

    public static function activate() {
        self::maybe_seed_default_config();
        self::register_rewrite_rules();
        flush_rewrite_rules();
        self::write_cache_file();
        self::write_fast_endpoint_files();
    }

    public static function deactivate() {
        flush_rewrite_rules();
    }

    public static function register_admin_menu() {
        add_menu_page(
            'App Config',
            'App Config',
            'manage_options',
            'woo-app-config-manager',
            [__CLASS__, 'render_admin_page'],
            'dashicons-admin-generic',
            58
        );
    }

    public static function register_setting() {
        register_setting(
            'woo_app_config_group',
            self::OPTION_KEY,
            [
                'type' => 'string',
                'sanitize_callback' => [__CLASS__, 'sanitize_json_input'],
                'default' => self::default_json(),
            ]
        );
    }

    public static function enqueue_admin_assets($hook) {
        if ($hook !== 'toplevel_page_woo-app-config-manager') {
            return;
        }

        wp_enqueue_media();

        $css = '
            .wam-wrap { max-width: 1300px; }
            .wam-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-top: 16px; }
            .wam-tabs { display: flex; gap: 10px; margin-top: 16px; }
            .wam-tab { text-decoration: none; border: 1px solid #d0d5dd; border-radius: 8px; padding: 7px 12px; color: #101828; background: #fff; }
            .wam-tab.active { background: #1570ef; color: #fff; border-color: #1570ef; }
            .wam-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 10px; box-shadow: 0 1px 2px rgba(0,0,0,.04); }
            .wam-card-head { padding: 14px 16px; border-bottom: 1px solid #eef2f7; }
            .wam-card-body { padding: 16px; }
            .wam-card.is-collapsed .wam-card-body { display: none; }
            .wam-collapse-toggle { cursor: pointer; display: inline-flex; align-items: center; gap: 8px; border: 0; background: transparent; padding: 0; font: inherit; color: inherit; }
            .wam-caret { display: inline-block; transition: transform .15s ease; }
            .wam-is-collapsed .wam-caret { transform: rotate(-90deg); }
            .wam-title { margin: 0; font-size: 16px; font-weight: 600; }
            .wam-sub { margin: 6px 0 0; color: #667085; }
            .wam-json { width: 100%; min-height: 620px; font-family: Menlo, Consolas, Monaco, monospace; font-size: 12px; line-height: 1.45; }
            .wam-status { display: inline-flex; align-items: center; gap: 6px; padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
            .wam-status.ok { background: #ecfdf3; color: #027a48; }
            .wam-status.bad { background: #fef3f2; color: #b42318; }
            .wam-meta { margin: 10px 0 0; color: #475467; }
            .wam-mono { font-family: Menlo, Consolas, Monaco, monospace; word-break: break-all; }
            .wam-actions { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 12px; }
            .wam-link-list { margin: 0; padding-left: 18px; }
            .wam-link-list li { margin: 6px 0; }
            .wam-field-grid { display: grid; grid-template-columns: repeat(2, minmax(220px, 1fr)); gap: 12px; }
            .wam-field { display: flex; flex-direction: column; gap: 6px; }
            .wam-field label { font-weight: 600; }
            .wam-field input[type="text"], .wam-field input[type="number"], .wam-field input[type="url"], .wam-field select, .wam-field textarea { width: 100%; }
            .wam-row-list { display: grid; gap: 12px; }
            .wam-row { border: 1px solid #e4e7ec; border-radius: 10px; background: #fcfcfd; }
            .wam-row-head { padding: 10px 12px; border-bottom: 1px solid #eaecf0; display: flex; justify-content: space-between; align-items: center; }
            .wam-row-body { padding: 12px; }
            .wam-row.is-collapsed .wam-row-body { display: none; }
            .wam-inline { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
            .wam-media-wrap { display: flex; gap: 8px; align-items: center; }
            .wam-hidden { display:none; }
            @media (max-width: 1180px) { .wam-grid { grid-template-columns: 1fr; } .wam-field-grid { grid-template-columns: 1fr; } }
        ';

        $js = '
            (function(){
                function bindMediaPicker(container){
                    container.querySelectorAll(".wam-pick-media").forEach(function(btn){
                        btn.addEventListener("click", function(e){
                            e.preventDefault();
                            var target = btn.closest(".wam-media-wrap").querySelector("input[type=url], input[type=text]");
                            var frame = wp.media({ title: "Select image", library: { type: "image" }, button: { text: "Use this image" }, multiple: false });
                            frame.on("select", function(){
                                var attachment = frame.state().get("selection").first().toJSON();
                                target.value = attachment.url || "";
                            });
                            frame.open();
                        });
                    });
                }

                function reindexRows(list){
                    list.querySelectorAll(".wam-row").forEach(function(row, idx){
                        row.querySelectorAll("[name]").forEach(function(el){
                            el.name = el.name.replace(/__INDEX__|\\[\\d+\\]/g, function(m){
                                if(m === "__INDEX__") return idx;
                                return "["+idx+"]";
                            });
                        });
                    });
                }

                function addRow(listSelector, templateSelector){
                    var list = document.querySelector(listSelector);
                    var tpl = document.querySelector(templateSelector);
                    if(!list || !tpl) return;
                    var clone = tpl.content.firstElementChild.cloneNode(true);
                    list.appendChild(clone);
                    reindexRows(list);
                    bindMediaPicker(clone);
                    bindAutoFill(clone);
                    bindCollapsible(clone);
                }

                function removeRow(btn){
                    var row = btn.closest(".wam-row");
                    var list = row.closest(".wam-row-list");
                    row.remove();
                    reindexRows(list);
                }

                function bindDynamicButtons(){
                    document.querySelectorAll(".wam-add-search-tab").forEach(function(btn){
                        btn.addEventListener("click", function(e){ e.preventDefault(); addRow("#wam-search-tab-list", "#wam-search-tab-template"); });
                    });
                    document.querySelectorAll(".wam-add-story").forEach(function(btn){
                        btn.addEventListener("click", function(e){ e.preventDefault(); addRow("#wam-story-list", "#wam-story-template"); });
                    });
                    document.querySelectorAll(".wam-add-banner").forEach(function(btn){
                        btn.addEventListener("click", function(e){ e.preventDefault(); addRow("#wam-banner-list", "#wam-banner-template"); });
                    });
                    document.querySelectorAll(".wam-add-image").forEach(function(btn){
                        btn.addEventListener("click", function(e){ e.preventDefault(); addRow("#wam-image-list", "#wam-image-template"); });
                    });
                    document.querySelectorAll(".wam-add-review").forEach(function(btn){
                        btn.addEventListener("click", function(e){ e.preventDefault(); addRow("#wam-review-list", "#wam-review-template"); });
                    });
                    document.addEventListener("click", function(e){
                        if(e.target.classList.contains("wam-remove-row")){
                            e.preventDefault();
                            removeRow(e.target);
                        }
                    });
                }

                function bindCollapsible(scope){
                    scope.querySelectorAll(".wam-collapse-toggle").forEach(function(btn){
                        if(btn.dataset.bound === "1") return;
                        btn.dataset.bound = "1";
                        btn.addEventListener("click", function(e){
                            e.preventDefault();
                            var container = btn.closest(".wam-card, .wam-row");
                            if(!container) return;
                            container.classList.toggle("is-collapsed");
                            btn.classList.toggle("wam-is-collapsed");
                        });
                    });
                }

                function bindAutoFill(scope){
                    scope.querySelectorAll(".wam-brand-target").forEach(function(sel){
                        sel.addEventListener("change", function(){
                            var row = sel.closest(".wam-row-body");
                            var target = row.querySelector(".wam-link-target");
                            if(target && sel.value){ target.value = sel.value; }
                        });
                    });
                    scope.querySelectorAll(".wam-attr-target").forEach(function(sel){
                        sel.addEventListener("change", function(){
                            var row = sel.closest(".wam-row-body");
                            var target = row.querySelector(".wam-link-target");
                            if(target && sel.value){ target.value = sel.value; }
                        });
                    });
                    scope.querySelectorAll(".wam-brand-source").forEach(function(sel){
                        sel.addEventListener("change", function(){
                            var row = sel.closest(".wam-row-body");
                            var target = row.querySelector(".wam-source");
                            if(!target) return;
                            var vals = Array.from(sel.selectedOptions).map(function(o){ return o.value; }).filter(Boolean);
                            target.value = vals.join(",");
                        });
                    });
                }

                document.addEventListener("DOMContentLoaded", function(){
                    bindMediaPicker(document);
                    bindDynamicButtons();
                    bindAutoFill(document);
                    bindCollapsible(document);
                });
            })();
        ';

        wp_register_style('wam-inline-style', false);
        wp_enqueue_style('wam-inline-style');
        wp_add_inline_style('wam-inline-style', $css);

        wp_add_inline_script('jquery', $js);
    }

    public static function sanitize_json_input($input) {
        if (!current_user_can('manage_options')) {
            add_settings_error(self::OPTION_KEY, 'forbidden', 'Permission denied.', 'error');
            return get_option(self::OPTION_KEY, self::default_json());
        }

        $input = trim((string) $input);
        if ($input === '') {
            add_settings_error(self::OPTION_KEY, 'empty', 'JSON cannot be empty.', 'error');
            return get_option(self::OPTION_KEY, self::default_json());
        }

        $decoded = json_decode($input, true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($decoded)) {
            add_settings_error(self::OPTION_KEY, 'invalid_json', 'Invalid JSON: ' . json_last_error_msg(), 'error');
            return get_option(self::OPTION_KEY, self::default_json());
        }

        self::ensure_base_keys($decoded);

        add_settings_error(self::OPTION_KEY, 'saved', 'Configuration saved successfully.', 'updated');

        return wp_json_encode($decoded, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    }

    public static function handle_option_updated($old_value, $value, $option) {
        self::write_cache_file();
        self::write_fast_endpoint_files();
    }

    public static function register_rest_routes() {
        register_rest_route(self::REST_NS, '/config', [
            'methods' => 'GET',
            'permission_callback' => '__return_true',
            'callback' => function () {
                return rest_ensure_response(self::get_config_array());
            },
        ]);
    }

    public static function register_query_vars($vars) {
        $vars[] = self::QUERY_VAR;
        return $vars;
    }

    public static function register_rewrite_rules() {
        add_rewrite_rule('^app/config-test\.php$', 'index.php?' . self::QUERY_VAR . '=1', 'top');
    }

    public static function handle_test_endpoint() {
        if ((int) get_query_var(self::QUERY_VAR, 0) !== 1) {
            return;
        }

        $payload = self::get_cached_or_live_json();
        if ($payload === null) {
            $payload = wp_json_encode(self::get_config_array(), JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        }

        nocache_headers();
        status_header(200);
        header('Content-Type: application/json; charset=utf-8');
        echo $payload;
        exit;
    }

    public static function render_admin_page() {
        if (!current_user_can('manage_options')) {
            wp_die('You do not have permission to access this page.');
        }

        self::maybe_seed_default_config();

        if (isset($_POST['wam_regenerate']) && check_admin_referer('wam_regenerate_cache', 'wam_nonce')) {
            self::write_cache_file();
            self::write_fast_endpoint_files();
            add_settings_error(self::OPTION_KEY, 'cache_regenerated', 'Cache file regenerated.', 'updated');
        }

        if (isset($_POST['wam_save_gui']) && check_admin_referer('wam_save_gui_nonce', 'wam_gui_nonce')) {
            $saved = self::save_from_gui($_POST);
            if ($saved) {
                add_settings_error(self::OPTION_KEY, 'gui_saved', 'GUI configuration saved successfully.', 'updated');
            } else {
                add_settings_error(self::OPTION_KEY, 'gui_failed', 'Failed to save GUI configuration.', 'error');
            }
        }

        settings_errors(self::OPTION_KEY);

        $active_tab = isset($_GET['tab']) && $_GET['tab'] === 'gui' ? 'gui' : 'json';

        $json = get_option(self::OPTION_KEY, self::default_json());
        $config = self::get_config_array();
        $is_valid = self::is_valid_json($json);

        $cache_file = self::cache_file_path();
        $cache_exists = file_exists($cache_file);
        $cache_url = self::cache_file_url();
        $fast_json_path = self::fast_json_path();
        $fast_php_path = self::fast_php_path();
        $fast_json_exists = $fast_json_path && file_exists($fast_json_path);
        $fast_php_exists = $fast_php_path && file_exists($fast_php_path);
        $fast_endpoint_url = home_url('/app/config-test.php');
        $fast_json_url = home_url('/app/config-test.json');
        $rest_url = rest_url(self::REST_NS . '/config');
        $test_url = home_url('/app/config-test.php');
        $updated = $cache_exists ? gmdate('Y-m-d H:i:s', (int) filemtime($cache_file)) . ' UTC' : 'Not generated yet';

        $brands = self::get_brand_options();
        $attrs = self::get_wc_attribute_options();
        $attr_terms = self::get_attribute_term_options();

        ?>
        <div class="wrap wam-wrap">
            <h1>Mobile App Config Manager</h1>
            <p>Manage app configuration from WP Admin and generate a cached JSON file. Existing <code>/app/config.php</code> remains untouched.</p>

            <div class="wam-tabs">
                <a class="wam-tab <?php echo $active_tab === 'json' ? 'active' : ''; ?>" href="<?php echo esc_url(admin_url('admin.php?page=woo-app-config-manager&tab=json')); ?>">JSON Editor</a>
                <a class="wam-tab <?php echo $active_tab === 'gui' ? 'active' : ''; ?>" href="<?php echo esc_url(admin_url('admin.php?page=woo-app-config-manager&tab=gui')); ?>">GUI Builder</a>
            </div>

            <div class="wam-grid">
                <div class="wam-card">
                    <div class="wam-card-head">
                        <h2 class="wam-title"><?php echo $active_tab === 'json' ? 'Configuration JSON Editor' : 'Configuration GUI Builder'; ?></h2>
                        <p class="wam-sub">
                            <?php if ($active_tab === 'json'): ?>
                                Paste or edit your JSON config. Save to validate and cache automatically.
                            <?php else: ?>
                                Use form controls to manage config sections. Save will regenerate JSON + cache.
                            <?php endif; ?>
                        </p>
                    </div>
                    <div class="wam-card-body">
                        <?php if ($active_tab === 'json'): ?>
                            <form method="post" action="options.php">
                                <?php settings_fields('woo_app_config_group'); ?>
                                <textarea class="wam-json" name="<?php echo esc_attr(self::OPTION_KEY); ?>" spellcheck="false"><?php echo esc_textarea($json); ?></textarea>
                                <div class="wam-actions">
                                    <?php submit_button('Save Configuration', 'primary', 'submit', false); ?>
                                </div>
                            </form>
                        <?php else: ?>
                            <?php self::render_gui_form($config, $brands, $attrs, $attr_terms); ?>
                        <?php endif; ?>
                    </div>
                </div>

                <div class="wam-card">
                    <div class="wam-card-head">
                        <h2 class="wam-title">Status & Endpoints</h2>
                        <p class="wam-sub">Quick health check and URLs for testing.</p>
                    </div>
                    <div class="wam-card-body">
                        <div>
                            <span class="wam-status <?php echo $is_valid ? 'ok' : 'bad'; ?>"><?php echo $is_valid ? 'JSON Valid' : 'JSON Invalid'; ?></span>
                        </div>

                        <p class="wam-meta"><strong>Cache File:</strong><br><span class="wam-mono"><?php echo esc_html($cache_file); ?></span></p>
                        <p class="wam-meta"><strong>Last Updated:</strong> <?php echo esc_html($updated); ?></p>
                        <p class="wam-meta"><strong>Fast Endpoint Files:</strong><br>
                            <span class="wam-mono"><?php echo esc_html($fast_php_path ? $fast_php_path : 'Unavailable'); ?></span><br>
                            <span class="wam-mono"><?php echo esc_html($fast_json_path ? $fast_json_path : 'Unavailable'); ?></span>
                        </p>

                        <form method="post" style="margin-top:12px;">
                            <?php wp_nonce_field('wam_regenerate_cache', 'wam_nonce'); ?>
                            <button type="submit" name="wam_regenerate" class="button">Regenerate Cache</button>
                        </form>

                        <hr style="margin:16px 0;">

                        <p><strong>Available Endpoints</strong></p>
                        <ul class="wam-link-list">
                            <?php if ($fast_php_exists): ?>
                            <li>Fast endpoint (no WP bootstrap):<br><a href="<?php echo esc_url($fast_endpoint_url); ?>" target="_blank" rel="noopener noreferrer" class="wam-mono"><?php echo esc_html($fast_endpoint_url); ?></a></li>
                            <?php endif; ?>
                            <?php if ($fast_json_exists): ?>
                            <li>Fast static JSON:<br><a href="<?php echo esc_url($fast_json_url); ?>" target="_blank" rel="noopener noreferrer" class="wam-mono"><?php echo esc_html($fast_json_url); ?></a></li>
                            <?php endif; ?>
                            <li>Test endpoint:<br><a href="<?php echo esc_url($test_url); ?>" target="_blank" rel="noopener noreferrer" class="wam-mono"><?php echo esc_html($test_url); ?></a></li>
                            <li>REST endpoint:<br><a href="<?php echo esc_url($rest_url); ?>" target="_blank" rel="noopener noreferrer" class="wam-mono"><?php echo esc_html($rest_url); ?></a></li>
                            <?php if ($cache_exists): ?>
                                <li>Direct cache file:<br><a href="<?php echo esc_url($cache_url); ?>" target="_blank" rel="noopener noreferrer" class="wam-mono"><?php echo esc_html($cache_url); ?></a></li>
                            <?php endif; ?>
                        </ul>

                        <p class="wam-meta" style="margin-top:14px;"><strong>Note:</strong> this plugin does not modify your existing <code>/app/config.php</code> endpoint.</p>
                    </div>
                </div>
            </div>
        </div>
        <?php
    }

    private static function render_gui_form($config, $brands, $attrs, $attr_terms) {
        $search_tabs = isset($config['search_tab']) && is_array($config['search_tab']) ? $config['search_tab'] : [];
        $stories = isset($config['stories']) && is_array($config['stories']) ? $config['stories'] : [];
        $banners = isset($config['home_banner']) && is_array($config['home_banner']) ? $config['home_banner'] : [];
        $images = isset($config['images']) && is_array($config['images']) ? $config['images'] : [];
        $review_criteria = isset($config['review_criteria']) && is_array($config['review_criteria']) ? $config['review_criteria'] : [];
        $shipping_options = self::get_shipping_method_options();
        $category_options = self::get_product_category_options();

        if (empty($search_tabs)) {
            $search_tabs[] = ['id' => 1, 'enabled' => true, 'title' => '', 'title_ar' => '', 'title_fa' => '', 'type' => 'attribute', 'source' => '', 'source_slug' => '', 'max' => '', 'veiw_type' => 'spotlight', 'more' => ['title' => '', 'link' => ['title' => '', 'type' => 'all_products', 'target' => '']]];
        }

        ?>
        <form method="post">
            <?php wp_nonce_field('wam_save_gui_nonce', 'wam_gui_nonce'); ?>

            <div class="wam-card is-collapsed" style="margin-bottom:12px;">
                <div class="wam-card-head">
                    <button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><h3 class="wam-title">General</h3></button>
                </div>
                <div class="wam-card-body wam-field-grid">
                    <div class="wam-field">
                        <label>Status</label>
                        <input type="text" name="cfg[status]" value="<?php echo esc_attr(self::g($config, ['status'], 'ok')); ?>">
                    </div>
                    <div class="wam-field">
                        <label>Message</label>
                        <input type="text" name="cfg[message]" value="<?php echo esc_attr(self::g($config, ['message'], '')); ?>">
                    </div>
                    <div class="wam-field" style="grid-column:1/-1;">
                        <label>Header Logo</label>
                        <div class="wam-media-wrap">
                            <input type="url" name="cfg[header_logo]" value="<?php echo esc_attr(self::g($config, ['header_logo'], '')); ?>" placeholder="https://...">
                            <button class="button wam-pick-media" type="button">Select from Media</button>
                        </div>
                    </div>
                    <div class="wam-field">
                        <label>Free Shipping Method</label>
                        <select name="cfg[free_shipping_method_id]">
                            <option value="">- Not Set -</option>
                            <?php foreach ($shipping_options as $opt) { echo '<option value="' . esc_attr($opt['id']) . '" ' . selected(self::g($config, ['free_shipping_method_id'], ''), $opt['id'], false) . '>' . esc_html($opt['label']) . '</option>'; } ?>
                        </select>
                    </div>
                </div>
            </div>

            <div class="wam-card is-collapsed" style="margin-bottom:12px;">
                <div class="wam-card-head">
                    <button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><h3 class="wam-title">Search Tab Rows</h3></button>
                </div>
                <div class="wam-card-body">
                    <div id="wam-search-tab-list" class="wam-row-list">
                        <?php foreach ($search_tabs as $i => $row) { self::render_search_tab_row($i, $row, $brands, $attrs, $attr_terms); } ?>
                    </div>
                    <div class="wam-actions"><button type="button" class="button wam-add-search-tab">+ Add Search Tab Row</button></div>
                </div>
            </div>

            <div class="wam-card is-collapsed" style="margin-bottom:12px;">
                <div class="wam-card-head">
                    <button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><h3 class="wam-title">Stories</h3></button>
                </div>
                <div class="wam-card-body">
                    <div id="wam-story-list" class="wam-row-list">
                        <?php foreach ($stories as $i => $row) { self::render_story_row($i, $row, $brands, $attr_terms); } ?>
                    </div>
                    <div class="wam-actions"><button type="button" class="button wam-add-story">+ Add Story</button></div>
                </div>
            </div>

            <div class="wam-card is-collapsed" style="margin-bottom:12px;">
                <div class="wam-card-head">
                    <button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><h3 class="wam-title">Home Banners</h3></button>
                </div>
                <div class="wam-card-body">
                    <div id="wam-banner-list" class="wam-row-list">
                        <?php foreach ($banners as $i => $row) { self::render_banner_row($i, $row, $brands, $attr_terms); } ?>
                    </div>
                    <div class="wam-actions"><button type="button" class="button wam-add-banner">+ Add Banner</button></div>
                </div>
            </div>

            <div class="wam-card is-collapsed" style="margin-bottom:12px;">
                <div class="wam-card-head">
                    <button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><h3 class="wam-title">Category Images</h3></button>
                </div>
                <div class="wam-card-body">
                    <div id="wam-image-list" class="wam-row-list">
                        <?php foreach ($images as $i => $row) { self::render_image_row($i, $row); } ?>
                    </div>
                    <div class="wam-actions"><button type="button" class="button wam-add-image">+ Add Image Row</button></div>
                </div>
            </div>

            <div class="wam-card is-collapsed" style="margin-bottom:12px;">
                <div class="wam-card-head">
                    <button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><h3 class="wam-title">Review Criteria</h3></button>
                </div>
                <div class="wam-card-body">
                    <div id="wam-review-list" class="wam-row-list">
                        <?php foreach ($review_criteria as $i => $row) { self::render_review_row($i, $row, $category_options); } ?>
                    </div>
                    <div class="wam-actions"><button type="button" class="button wam-add-review">+ Add Review Criteria Row</button></div>
                </div>
            </div>

            <div class="wam-card is-collapsed" style="margin-bottom:12px;">
                <div class="wam-card-head">
                    <button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><h3 class="wam-title">App Version & Contact</h3></button>
                </div>
                <div class="wam-card-body wam-field-grid">
                    <div class="wam-field"><label>Latest Version</label><input type="text" name="cfg[app_version][latest_version]" value="<?php echo esc_attr(self::g($config, ['app_version', 'latest_version'], '')); ?>"></div>
                    <div class="wam-field"><label>Min Required Version</label><input type="text" name="cfg[app_version][min_required_version]" value="<?php echo esc_attr(self::g($config, ['app_version', 'min_required_version'], '')); ?>"></div>
                    <div class="wam-field"><label>Call</label><input type="text" name="cfg[contact][call]" value="<?php echo esc_attr(self::g($config, ['contact', 'call'], '')); ?>"></div>
                    <div class="wam-field"><label>WhatsApp</label><input type="url" name="cfg[contact][whatsapp]" value="<?php echo esc_attr(self::g($config, ['contact', 'whatsapp'], '')); ?>"></div>
                    <div class="wam-field"><label>Instagram</label><input type="url" name="cfg[contact][instagram]" value="<?php echo esc_attr(self::g($config, ['contact', 'instagram'], '')); ?>"></div>
                    <div class="wam-field"><label>Telegram</label><input type="url" name="cfg[contact][telegram]" value="<?php echo esc_attr(self::g($config, ['contact', 'telegram'], '')); ?>"></div>
                    <div class="wam-field"><label>Email</label><input type="email" name="cfg[contact][email]" value="<?php echo esc_attr(self::g($config, ['contact', 'email'], '')); ?>"></div>
                </div>
            </div>

            <div class="wam-actions">
                <button type="submit" name="wam_save_gui" class="button button-primary">Save GUI Configuration</button>
            </div>
        </form>

        <template id="wam-search-tab-template">
            <?php self::render_search_tab_row('__INDEX__', ['id' => '', 'enabled' => true, 'title' => '', 'title_ar' => '', 'title_fa' => '', 'type' => 'attribute', 'source' => '', 'source_slug' => '', 'max' => '', 'veiw_type' => 'spotlight', 'more' => ['title' => '', 'link' => ['title' => '', 'type' => 'all_products', 'target' => '']]], $brands, $attrs, $attr_terms); ?>
        </template>

        <template id="wam-story-template">
            <?php self::render_story_row('__INDEX__', ['id' => '', 'title' => '', 'subtitle' => '', 'media_url' => '', 'link' => ['title' => '', 'type' => 'products', 'target' => '']], $brands, $attr_terms); ?>
        </template>

        <template id="wam-banner-template">
            <?php self::render_banner_row('__INDEX__', ['id' => '', 'title' => '', 'subtitle' => '', 'src' => '', 'link' => ['title' => '', 'type' => 'products', 'target' => '']], $brands, $attr_terms); ?>
        </template>
        <template id="wam-image-template">
            <?php self::render_image_row('__INDEX__', ['term_id' => '', 'src' => '']); ?>
        </template>
        <template id="wam-review-template">
            <?php self::render_review_row('__INDEX__', ['cat_id' => '', 'criteria' => []], $category_options); ?>
        </template>
        <?php
    }

    private static function render_search_tab_row($i, $row, $brands, $attrs, $attr_terms) {
        $prefix = 'cfg[search_tab][' . $i . ']';
        ?>
        <div class="wam-row is-collapsed">
            <div class="wam-row-head"><button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><strong>Search Row</strong></button><button type="button" class="button-link-delete wam-remove-row">Remove</button></div>
            <div class="wam-row-body">
                <div class="wam-field-grid">
                    <div class="wam-field"><label>ID</label><input type="number" name="<?php echo esc_attr($prefix); ?>[id]" value="<?php echo esc_attr(self::g($row, ['id'], '')); ?>"></div>
                    <div class="wam-field"><label>Enabled</label><select name="<?php echo esc_attr($prefix); ?>[enabled]"><?php self::option_bool(self::g($row, ['enabled'], true)); ?></select></div>
                    <div class="wam-field"><label>Title (EN)</label><input type="text" name="<?php echo esc_attr($prefix); ?>[title]" value="<?php echo esc_attr(self::g($row, ['title'], '')); ?>"></div>
                    <div class="wam-field"><label>Title (AR)</label><input type="text" name="<?php echo esc_attr($prefix); ?>[title_ar]" value="<?php echo esc_attr(self::g($row, ['title_ar'], '')); ?>"></div>
                    <div class="wam-field"><label>Title (FA)</label><input type="text" name="<?php echo esc_attr($prefix); ?>[title_fa]" value="<?php echo esc_attr(self::g($row, ['title_fa'], '')); ?>"></div>

                    <div class="wam-field"><label>Type</label><select name="<?php echo esc_attr($prefix); ?>[type]"><?php self::options(self::g($row, ['type'], 'attribute'), ['attribute'=>'attribute','attributes'=>'attributes','brand_ids'=>'brand_ids','products'=>'products','categories'=>'categories','tag'=>'tag']); ?></select></div>
                    <div class="wam-field"><label>View Type</label><select name="<?php echo esc_attr($prefix); ?>[veiw_type]"><?php self::options(self::g($row, ['veiw_type'], 'spotlight'), ['spotlight'=>'spotlight','circle_row'=>'circle_row','grid'=>'grid','list'=>'list']); ?></select></div>

                    <div class="wam-field"><label>Source (manual)</label><input class="wam-source" type="text" name="<?php echo esc_attr($prefix); ?>[source]" value="<?php echo esc_attr(self::g($row, ['source'], '')); ?>" placeholder="e.g. 67,163,71"></div>
                    <div class="wam-field"><label>Source Slug (Attribute)</label><select name="<?php echo esc_attr($prefix); ?>[source_slug]"><option value="">- Select -</option><?php foreach ($attrs as $a) { echo '<option value="' . esc_attr($a['slug']) . '" ' . selected(self::g($row, ['source_slug'], ''), $a['slug'], false) . '>' . esc_html($a['name']) . ' (' . esc_html($a['slug']) . ')</option>'; } ?></select></div>
                    <div class="wam-field"><label>Source Brands (auto-fill Source)</label><select class="wam-brand-source" multiple><?php foreach ($brands as $b) { echo '<option value="' . esc_attr($b['id']) . '">' . esc_html($b['name']) . '</option>'; } ?></select></div>
                    <div class="wam-field"><label>Max</label><input type="number" name="<?php echo esc_attr($prefix); ?>[max]" value="<?php echo esc_attr(self::g($row, ['max'], '')); ?>"></div>

                    <div class="wam-field"><label>More Title</label><input type="text" name="<?php echo esc_attr($prefix); ?>[more][title]" value="<?php echo esc_attr(self::g($row, ['more', 'title'], '')); ?>"></div>
                    <div class="wam-field"><label>More Link Title</label><input type="text" name="<?php echo esc_attr($prefix); ?>[more][link][title]" value="<?php echo esc_attr(self::g($row, ['more', 'link', 'title'], '')); ?>"></div>
                    <div class="wam-field"><label>More Link Type</label><select name="<?php echo esc_attr($prefix); ?>[more][link][type]"><?php self::options(self::g($row, ['more', 'link', 'type'], 'all_products'), ['all_products'=>'all_products','all_brands'=>'all_brands','products'=>'products','categories'=>'categories','brand'=>'brand','attribute'=>'attribute','attributes'=>'attributes','tag'=>'tag']); ?></select></div>
                    <div class="wam-field"><label>More Link Target</label><input class="wam-link-target" type="text" name="<?php echo esc_attr($prefix); ?>[more][link][target]" value="<?php echo esc_attr(self::g($row, ['more', 'link', 'target'], '')); ?>" placeholder="e.g. 67 or scent:123"></div>
                    <div class="wam-field"><label>Select Brand for Target</label><select class="wam-brand-target"><option value="">- Select -</option><?php foreach ($brands as $b) { echo '<option value="' . esc_attr($b['id']) . '">' . esc_html($b['name']) . '</option>'; } ?></select></div>
                    <div class="wam-field"><label>Select Attribute Term for Target</label><select class="wam-attr-target"><option value="">- Select -</option><?php foreach ($attr_terms as $t) { echo '<option value="' . esc_attr($t['value']) . '">' . esc_html($t['label']) . '</option>'; } ?></select></div>
                </div>
            </div>
        </div>
        <?php
    }

    private static function render_story_row($i, $row, $brands, $attr_terms) {
        $prefix = 'cfg[stories][' . $i . ']';
        ?>
        <div class="wam-row is-collapsed">
            <div class="wam-row-head"><button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><strong>Story</strong></button><button type="button" class="button-link-delete wam-remove-row">Remove</button></div>
            <div class="wam-row-body">
                <div class="wam-field-grid">
                    <div class="wam-field"><label>ID</label><input type="number" name="<?php echo esc_attr($prefix); ?>[id]" value="<?php echo esc_attr(self::g($row, ['id'], '')); ?>"></div>
                    <div class="wam-field"><label>Title</label><input type="text" name="<?php echo esc_attr($prefix); ?>[title]" value="<?php echo esc_attr(self::g($row, ['title'], '')); ?>"></div>
                    <div class="wam-field"><label>Subtitle</label><input type="text" name="<?php echo esc_attr($prefix); ?>[subtitle]" value="<?php echo esc_attr(self::g($row, ['subtitle'], '')); ?>"></div>
                    <div class="wam-field"><label>Link Title</label><input type="text" name="<?php echo esc_attr($prefix); ?>[link][title]" value="<?php echo esc_attr(self::g($row, ['link', 'title'], '')); ?>"></div>
                    <div class="wam-field"><label>Link Type</label><select name="<?php echo esc_attr($prefix); ?>[link][type]"><?php self::options(self::g($row, ['link', 'type'], 'products'), ['products'=>'products','categories'=>'categories','brand'=>'brand','attribute'=>'attribute','attributes'=>'attributes','tag'=>'tag','all_products'=>'all_products']); ?></select></div>
                    <div class="wam-field"><label>Link Target</label><input class="wam-link-target" type="text" name="<?php echo esc_attr($prefix); ?>[link][target]" value="<?php echo esc_attr(self::g($row, ['link', 'target'], '')); ?>"></div>
                    <div class="wam-field"><label>Select Brand for Target</label><select class="wam-brand-target"><option value="">- Select -</option><?php foreach ($brands as $b) { echo '<option value="' . esc_attr($b['id']) . '">' . esc_html($b['name']) . '</option>'; } ?></select></div>
                    <div class="wam-field"><label>Select Attribute Term for Target</label><select class="wam-attr-target"><option value="">- Select -</option><?php foreach ($attr_terms as $t) { echo '<option value="' . esc_attr($t['value']) . '">' . esc_html($t['label']) . '</option>'; } ?></select></div>
                    <div class="wam-field" style="grid-column:1/-1;"><label>Media URL</label><div class="wam-media-wrap"><input type="url" name="<?php echo esc_attr($prefix); ?>[media_url]" value="<?php echo esc_attr(self::g($row, ['media_url'], '')); ?>"><button type="button" class="button wam-pick-media">Select from Media</button></div></div>
                </div>
            </div>
        </div>
        <?php
    }

    private static function render_banner_row($i, $row, $brands, $attr_terms) {
        $prefix = 'cfg[home_banner][' . $i . ']';
        ?>
        <div class="wam-row is-collapsed">
            <div class="wam-row-head"><button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><strong>Banner</strong></button><button type="button" class="button-link-delete wam-remove-row">Remove</button></div>
            <div class="wam-row-body">
                <div class="wam-field-grid">
                    <div class="wam-field"><label>ID</label><input type="number" name="<?php echo esc_attr($prefix); ?>[id]" value="<?php echo esc_attr(self::g($row, ['id'], '')); ?>"></div>
                    <div class="wam-field"><label>Title</label><input type="text" name="<?php echo esc_attr($prefix); ?>[title]" value="<?php echo esc_attr(self::g($row, ['title'], '')); ?>"></div>
                    <div class="wam-field"><label>Subtitle</label><input type="text" name="<?php echo esc_attr($prefix); ?>[subtitle]" value="<?php echo esc_attr(self::g($row, ['subtitle'], '')); ?>"></div>
                    <div class="wam-field"><label>Link Title</label><input type="text" name="<?php echo esc_attr($prefix); ?>[link][title]" value="<?php echo esc_attr(self::g($row, ['link', 'title'], '')); ?>"></div>
                    <div class="wam-field"><label>Link Type</label><select name="<?php echo esc_attr($prefix); ?>[link][type]"><?php self::options(self::g($row, ['link', 'type'], 'products'), ['products'=>'products','categories'=>'categories','brand'=>'brand','attribute'=>'attribute','attributes'=>'attributes','tag'=>'tag','all_products'=>'all_products']); ?></select></div>
                    <div class="wam-field"><label>Link Target</label><input class="wam-link-target" type="text" name="<?php echo esc_attr($prefix); ?>[link][target]" value="<?php echo esc_attr(self::g($row, ['link', 'target'], '')); ?>"></div>
                    <div class="wam-field"><label>Select Brand for Target</label><select class="wam-brand-target"><option value="">- Select -</option><?php foreach ($brands as $b) { echo '<option value="' . esc_attr($b['id']) . '">' . esc_html($b['name']) . '</option>'; } ?></select></div>
                    <div class="wam-field"><label>Select Attribute Term for Target</label><select class="wam-attr-target"><option value="">- Select -</option><?php foreach ($attr_terms as $t) { echo '<option value="' . esc_attr($t['value']) . '">' . esc_html($t['label']) . '</option>'; } ?></select></div>
                    <div class="wam-field" style="grid-column:1/-1;"><label>Image Src</label><div class="wam-media-wrap"><input type="url" name="<?php echo esc_attr($prefix); ?>[src]" value="<?php echo esc_attr(self::g($row, ['src'], '')); ?>"><button type="button" class="button wam-pick-media">Select from Media</button></div></div>
                </div>
            </div>
        </div>
        <?php
    }

    private static function render_image_row($i, $row) {
        $prefix = 'cfg[images][' . $i . ']';
        ?>
        <div class="wam-row is-collapsed">
            <div class="wam-row-head"><button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><strong>Image Row</strong></button><button type="button" class="button-link-delete wam-remove-row">Remove</button></div>
            <div class="wam-row-body">
                <div class="wam-field-grid">
                    <div class="wam-field"><label>Term ID</label><input type="number" name="<?php echo esc_attr($prefix); ?>[term_id]" value="<?php echo esc_attr(self::g($row, ['term_id'], '')); ?>"></div>
                    <div class="wam-field" style="grid-column:1/-1;"><label>Image Src</label><div class="wam-media-wrap"><input type="url" name="<?php echo esc_attr($prefix); ?>[src]" value="<?php echo esc_attr(self::g($row, ['src'], '')); ?>"><button type="button" class="button wam-pick-media">Select from Media</button></div></div>
                </div>
            </div>
        </div>
        <?php
    }

    private static function render_review_row($i, $row, $categories) {
        $prefix = 'cfg[review_criteria][' . $i . ']';
        $criteria = self::g($row, ['criteria'], []);
        $criteriaText = is_array($criteria) ? implode("\n", $criteria) : '';
        $criteriaAr = self::g($row, ['criteria_ar'], []);
        $criteriaArText = is_array($criteriaAr) ? implode("\n", $criteriaAr) : '';
        $criteriaFa = self::g($row, ['criteria_fa'], []);
        $criteriaFaText = is_array($criteriaFa) ? implode("\n", $criteriaFa) : '';
        ?>
        <div class="wam-row is-collapsed">
            <div class="wam-row-head"><button type="button" class="wam-collapse-toggle wam-is-collapsed"><span class="wam-caret">▾</span><strong>Review Criteria Row</strong></button><button type="button" class="button-link-delete wam-remove-row">Remove</button></div>
            <div class="wam-row-body">
                <div class="wam-field-grid">
                    <div class="wam-field">
                        <label>Category</label>
                        <select name="<?php echo esc_attr($prefix); ?>[cat_id]">
                            <option value="">- Select Category -</option>
                            <?php foreach ($categories as $cat) { echo '<option value="' . esc_attr($cat['id']) . '" ' . selected((string) self::g($row, ['cat_id'], ''), (string) $cat['id'], false) . '>' . esc_html($cat['name']) . ' (#' . esc_html((string) $cat['id']) . ')</option>'; } ?>
                        </select>
                    </div>
                    <div class="wam-field"><label>Category ID (manual)</label><input type="number" name="<?php echo esc_attr($prefix); ?>[cat_id_manual]" value=""></div>
                    <div class="wam-field" style="grid-column:1/-1;">
                        <label>Criteria EN/Default (one per line)</label>
                        <textarea rows="5" name="<?php echo esc_attr($prefix); ?>[criteria_text]"><?php echo esc_textarea($criteriaText); ?></textarea>
                    </div>
                    <div class="wam-field" style="grid-column:1/-1;">
                        <label>Criteria AR (one per line)</label>
                        <textarea rows="5" name="<?php echo esc_attr($prefix); ?>[criteria_ar_text]"><?php echo esc_textarea($criteriaArText); ?></textarea>
                    </div>
                    <div class="wam-field" style="grid-column:1/-1;">
                        <label>Criteria FA (one per line)</label>
                        <textarea rows="5" name="<?php echo esc_attr($prefix); ?>[criteria_fa_text]"><?php echo esc_textarea($criteriaFaText); ?></textarea>
                    </div>
                </div>
            </div>
        </div>
        <?php
    }

    private static function save_from_gui($post) {
        if (!isset($post['cfg']) || !is_array($post['cfg'])) {
            return false;
        }

        $cfg = $post['cfg'];
        $existing = self::get_config_array();

        $existing['status'] = self::s(self::g($cfg, ['status'], 'ok'));
        $existing['message'] = self::s(self::g($cfg, ['message'], 'Config managed from WordPress admin'));
        $existing['header_logo'] = self::u(self::g($cfg, ['header_logo'], ''));
        $existing['free_shipping_method_id'] = self::s(self::g($cfg, ['free_shipping_method_id'], ''));

        $existing['app_version'] = [
            'latest_version' => self::s(self::g($cfg, ['app_version', 'latest_version'], '')),
            'min_required_version' => self::s(self::g($cfg, ['app_version', 'min_required_version'], '')),
        ];

        $existing['contact'] = [
            'call' => self::s(self::g($cfg, ['contact', 'call'], '')),
            'whatsapp' => self::u(self::g($cfg, ['contact', 'whatsapp'], '')),
            'instagram' => self::u(self::g($cfg, ['contact', 'instagram'], '')),
            'telegram' => self::u(self::g($cfg, ['contact', 'telegram'], '')),
            'email' => sanitize_email(self::g($cfg, ['contact', 'email'], '')),
        ];

        $existing['search_tab'] = [];
        $searchRows = self::g($cfg, ['search_tab'], []);
        if (is_array($searchRows)) {
            foreach ($searchRows as $row) {
                if (!is_array($row)) {
                    continue;
                }
                $item = [
                    'id' => self::i(self::g($row, ['id'], 0)),
                    'enabled' => self::b(self::g($row, ['enabled'], true)),
                    'title' => self::s(self::g($row, ['title'], '')),
                    'title_ar' => self::s(self::g($row, ['title_ar'], '')),
                    'title_fa' => self::s(self::g($row, ['title_fa'], '')),
                    'type' => self::s(self::g($row, ['type'], 'attribute')),
                    'source' => self::s(self::g($row, ['source'], '')),
                    'source_slug' => self::s(self::g($row, ['source_slug'], '')),
                    'max' => self::i(self::g($row, ['max'], 0)),
                    'veiw_type' => self::s(self::g($row, ['veiw_type'], 'spotlight')),
                    'more' => [
                        'title' => self::s(self::g($row, ['more', 'title'], '')),
                        'link' => [
                            'title' => self::s(self::g($row, ['more', 'link', 'title'], '')),
                            'type' => self::s(self::g($row, ['more', 'link', 'type'], 'all_products')),
                            'target' => self::s(self::g($row, ['more', 'link', 'target'], '')),
                        ],
                    ],
                ];

                if ($item['id'] > 0 || $item['title'] !== '') {
                    $existing['search_tab'][] = $item;
                }
            }
        }

        $existing['stories'] = self::sanitize_linked_media_rows(self::g($cfg, ['stories'], []), 'media_url');
        $existing['home_banner'] = self::sanitize_linked_media_rows(self::g($cfg, ['home_banner'], []), 'src');
        $existing['images'] = [];
        $imageRows = self::g($cfg, ['images'], []);
        if (is_array($imageRows)) {
            foreach ($imageRows as $row) {
                if (!is_array($row)) {
                    continue;
                }
                $item = [
                    'term_id' => self::i(self::g($row, ['term_id'], 0)),
                    'src' => self::u(self::g($row, ['src'], '')),
                ];
                if ($item['term_id'] > 0 || $item['src'] !== '') {
                    $existing['images'][] = $item;
                }
            }
        }

        $existing['review_criteria'] = [];
        $reviewRows = self::g($cfg, ['review_criteria'], []);
        if (is_array($reviewRows)) {
            foreach ($reviewRows as $row) {
                if (!is_array($row)) {
                    continue;
                }
                $catId = self::i(self::g($row, ['cat_id'], 0));
                $catManual = self::i(self::g($row, ['cat_id_manual'], 0));
                if ($catManual > 0) {
                    $catId = $catManual;
                }
                $criteriaText = (string) self::g($row, ['criteria_text'], '');
                $criteria = array_values(array_filter(array_map('trim', preg_split('/\\r\\n|\\r|\\n/', $criteriaText))));
                $criteriaArText = (string) self::g($row, ['criteria_ar_text'], '');
                $criteriaAr = array_values(array_filter(array_map('trim', preg_split('/\\r\\n|\\r|\\n/', $criteriaArText))));
                $criteriaFaText = (string) self::g($row, ['criteria_fa_text'], '');
                $criteriaFa = array_values(array_filter(array_map('trim', preg_split('/\\r\\n|\\r|\\n/', $criteriaFaText))));

                if ($catId > 0 || !empty($criteria) || !empty($criteriaAr) || !empty($criteriaFa)) {
                    $item = [
                        'cat_id' => $catId,
                        'criteria' => $criteria,
                    ];
                    if (!empty($criteriaAr)) {
                        $item['criteria_ar'] = $criteriaAr;
                    }
                    if (!empty($criteriaFa)) {
                        $item['criteria_fa'] = $criteriaFa;
                    }
                    $existing['review_criteria'][] = $item;
                }
            }
        }

        self::ensure_base_keys($existing);

        $json = wp_json_encode($existing, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if (!$json) {
            return false;
        }

        $ok = update_option(self::OPTION_KEY, $json, false);
        self::write_cache_file();

        return $ok !== false || true;
    }

    private static function sanitize_linked_media_rows($rows, $mediaKey) {
        $out = [];
        if (!is_array($rows)) {
            return $out;
        }

        foreach ($rows as $row) {
            if (!is_array($row)) {
                continue;
            }
            $item = [
                'id' => self::i(self::g($row, ['id'], 0)),
                'title' => self::s(self::g($row, ['title'], '')),
                'subtitle' => self::s(self::g($row, ['subtitle'], '')),
                $mediaKey => self::u(self::g($row, [$mediaKey], '')),
                'link' => [
                    'title' => self::s(self::g($row, ['link', 'title'], '')),
                    'type' => self::s(self::g($row, ['link', 'type'], 'products')),
                    'target' => self::s(self::g($row, ['link', 'target'], '')),
                ],
            ];

            if ($item['id'] > 0 || $item['title'] !== '' || $item[$mediaKey] !== '') {
                $out[] = $item;
            }
        }

        return $out;
    }

    private static function ensure_base_keys(&$decoded) {
        if (!isset($decoded['status'])) {
            $decoded['status'] = 'ok';
        }
        if (!isset($decoded['message'])) {
            $decoded['message'] = 'Config generated from WordPress admin.';
        }
        foreach (['stories', 'search_tab', 'home_banner', 'images', 'review_criteria', 'payment_discount', 'payment_force_enabled'] as $k) {
            if (!isset($decoded[$k]) || !is_array($decoded[$k])) {
                $decoded[$k] = [];
            }
        }
        if (!isset($decoded['free_shipping_method_id'])) {
            $decoded['free_shipping_method_id'] = '';
        }
        if (!isset($decoded['bacs_details']) || !is_array($decoded['bacs_details'])) {
            $decoded['bacs_details'] = ['card_number' => '', 'iban_number' => '', 'account_holder' => '', 'contact_number' => ''];
        }
        if (!isset($decoded['app_version']) || !is_array($decoded['app_version'])) {
            $decoded['app_version'] = ['latest_version' => '1.0', 'min_required_version' => '1.0'];
        }
        if (!isset($decoded['contact']) || !is_array($decoded['contact'])) {
            $decoded['contact'] = ['call' => '', 'whatsapp' => '', 'instagram' => '', 'telegram' => '', 'email' => ''];
        }
    }

    private static function maybe_seed_default_config() {
        if (get_option(self::OPTION_KEY, null) === null) {
            add_option(self::OPTION_KEY, self::default_json());
        }
    }

    private static function default_json() {
        $default = [
            'status' => 'ok',
            'message' => 'Config managed from WordPress admin',
            'header_logo' => '',
            'payment_discount' => [],
            'payment_force_enabled' => [],
            'bacs_details' => ['card_number' => '', 'iban_number' => '', 'account_holder' => '', 'contact_number' => ''],
            'stories' => [],
            'search_tab' => [],
            'home_banner' => [],
            'images' => [],
            'free_shipping_method_id' => '',
            'review_criteria' => [],
            'app_version' => ['latest_version' => '1.0', 'min_required_version' => '1.0'],
            'contact' => ['call' => '', 'whatsapp' => '', 'instagram' => '', 'telegram' => '', 'email' => ''],
        ];

        return wp_json_encode($default, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    }

    private static function get_config_array() {
        $json = get_option(self::OPTION_KEY, self::default_json());
        $decoded = json_decode((string) $json, true);
        return is_array($decoded) ? $decoded : [];
    }

    private static function is_valid_json($json) {
        json_decode((string) $json, true);
        return json_last_error() === JSON_ERROR_NONE;
    }

    private static function cache_dir_path() {
        $upload = wp_upload_dir();
        return trailingslashit($upload['basedir']) . 'woo-app-config';
    }

    private static function cache_file_path() {
        return trailingslashit(self::cache_dir_path()) . 'app-config.json';
    }

    private static function cache_file_url() {
        $upload = wp_upload_dir();
        return trailingslashit($upload['baseurl']) . 'woo-app-config/app-config.json';
    }

    private static function get_cached_or_live_json() {
        $path = self::cache_file_path();
        if (!file_exists($path)) {
            return null;
        }
        $contents = file_get_contents($path);
        return $contents !== false ? $contents : null;
    }

    private static function write_cache_file() {
        $dir = self::cache_dir_path();
        if (!file_exists($dir)) {
            wp_mkdir_p($dir);
        }

        $json = get_option(self::OPTION_KEY, self::default_json());
        if (!self::is_valid_json($json)) {
            return false;
        }

        $file = self::cache_file_path();
        return file_put_contents($file, $json) !== false;
    }

    private static function fast_dir_path() {
        if (!defined('ABSPATH')) {
            return null;
        }
        return trailingslashit(ABSPATH) . self::FAST_DIR_RELATIVE;
    }

    private static function fast_json_path() {
        $dir = self::fast_dir_path();
        if (!$dir) {
            return null;
        }
        return trailingslashit($dir) . self::FAST_JSON_FILE;
    }

    private static function fast_php_path() {
        $dir = self::fast_dir_path();
        if (!$dir) {
            return null;
        }
        return trailingslashit($dir) . self::FAST_PHP_FILE;
    }

    private static function write_fast_endpoint_files() {
        $dir = self::fast_dir_path();
        $jsonPath = self::fast_json_path();
        $phpPath = self::fast_php_path();
        if (!$dir || !$jsonPath || !$phpPath) {
            return false;
        }
        if (!file_exists($dir)) {
            @wp_mkdir_p($dir);
        }
        if (!file_exists($dir) || !is_dir($dir) || !is_writable($dir)) {
            return false;
        }

        $json = get_option(self::OPTION_KEY, self::default_json());
        if (!self::is_valid_json($json)) {
            return false;
        }

        $jsonOk = @file_put_contents($jsonPath, $json) !== false;
        if (!$jsonOk) {
            return false;
        }

        $phpStub = "<?php\n";
        $phpStub .= "header('Content-Type: application/json; charset=utf-8');\n";
        $phpStub .= "header('Cache-Control: public, max-age=300, s-maxage=300');\n";
        $phpStub .= "\$f = __DIR__ . '/" . self::FAST_JSON_FILE . "';\n";
        $phpStub .= "if (!is_file(\$f)) { http_response_code(404); echo '{\"status\":\"error\",\"message\":\"config file not found\"}'; exit; }\n";
        $phpStub .= "readfile(\$f);\n";
        $phpStub .= "exit;\n";

        return @file_put_contents($phpPath, $phpStub) !== false;
    }

    private static function get_shipping_method_options() {
        $out = [];
        if (!class_exists('WC_Shipping_Zones')) {
            return $out;
        }

        $zones = WC_Shipping_Zones::get_zones();
        if (is_array($zones)) {
            foreach ($zones as $zoneData) {
                if (!isset($zoneData['shipping_methods']) || !is_array($zoneData['shipping_methods'])) {
                    continue;
                }
                foreach ($zoneData['shipping_methods'] as $method) {
                    if (!is_object($method) || !isset($method->id)) {
                        continue;
                    }
                    $id = isset($method->instance_id) ? (string) $method->instance_id : (string) $method->id;
                    $label = (isset($method->method_title) ? $method->method_title : $method->id) . ' (' . $method->id . ':' . $id . ')';
                    $out[] = ['id' => $id, 'label' => $label];
                }
            }
        }

        if (empty($out)) {
            $out[] = ['id' => 'free_shipping', 'label' => 'free_shipping'];
        }

        return $out;
    }

    private static function get_product_category_options() {
        $out = [];
        if (!taxonomy_exists('product_cat')) {
            return $out;
        }
        $terms = get_terms([
            'taxonomy' => 'product_cat',
            'hide_empty' => false,
        ]);
        if (is_wp_error($terms) || !is_array($terms)) {
            return $out;
        }
        foreach ($terms as $t) {
            $out[] = ['id' => (int) $t->term_id, 'name' => $t->name];
        }
        return $out;
    }

    private static function get_wc_attribute_options() {
        $out = [];
        if (function_exists('wc_get_attribute_taxonomies')) {
            $taxes = wc_get_attribute_taxonomies();
            if (is_array($taxes)) {
                foreach ($taxes as $tax) {
                    $out[] = [
                        'id' => isset($tax->attribute_id) ? (int) $tax->attribute_id : 0,
                        'slug' => isset($tax->attribute_name) ? (string) $tax->attribute_name : '',
                        'name' => isset($tax->attribute_label) ? (string) $tax->attribute_label : (isset($tax->attribute_name) ? (string) $tax->attribute_name : ''),
                    ];
                }
            }
        }
        return $out;
    }

    private static function get_attribute_term_options() {
        $items = [];
        $attrs = self::get_wc_attribute_options();
        foreach ($attrs as $attr) {
            if (empty($attr['slug'])) {
                continue;
            }
            $taxonomy = 'pa_' . $attr['slug'];
            if (!taxonomy_exists($taxonomy)) {
                continue;
            }
            $terms = get_terms([
                'taxonomy' => $taxonomy,
                'hide_empty' => false,
            ]);
            if (is_wp_error($terms) || !is_array($terms)) {
                continue;
            }
            foreach ($terms as $t) {
                $items[] = [
                    'value' => $attr['slug'] . ':' . (int) $t->term_id,
                    'label' => $attr['name'] . ' -> ' . $t->name . ' (#' . (int) $t->term_id . ')',
                ];
            }
        }
        return $items;
    }

    private static function get_brand_options() {
        $taxCandidates = ['product_brand', 'pwb-brand', 'yith_product_brand', 'brand'];
        foreach ($taxCandidates as $tax) {
            if (!taxonomy_exists($tax)) {
                continue;
            }
            $terms = get_terms([
                'taxonomy' => $tax,
                'hide_empty' => false,
            ]);
            if (is_wp_error($terms) || !is_array($terms)) {
                continue;
            }

            $out = [];
            foreach ($terms as $t) {
                $out[] = ['id' => (int) $t->term_id, 'name' => $t->name];
            }
            return $out;
        }
        return [];
    }

    private static function g($arr, $path, $default = '') {
        $cur = $arr;
        foreach ($path as $k) {
            if (!is_array($cur) || !array_key_exists($k, $cur)) {
                return $default;
            }
            $cur = $cur[$k];
        }
        return $cur;
    }

    private static function s($v) {
        return sanitize_text_field((string) $v);
    }

    private static function u($v) {
        return esc_url_raw((string) $v);
    }

    private static function i($v) {
        return (int) $v;
    }

    private static function b($v) {
        return in_array((string) $v, ['1', 'true', 'yes', 'on'], true) || $v === true || $v === 1;
    }

    private static function option_bool($selected) {
        echo '<option value="1" ' . selected((bool) $selected, true, false) . '>true</option>';
        echo '<option value="0" ' . selected((bool) $selected, false, false) . '>false</option>';
    }

    private static function options($selected, $options) {
        foreach ($options as $value => $label) {
            echo '<option value="' . esc_attr($value) . '" ' . selected((string) $selected, (string) $value, false) . '>' . esc_html($label) . '</option>';
        }
    }
}

Woo_App_Config_Manager::init();
