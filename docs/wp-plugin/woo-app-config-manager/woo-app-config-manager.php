<?php
/**
 * Plugin Name: Woo App Config Manager
 * Description: Manage mobile app configuration from WordPress admin, cache JSON to a file, and expose a test endpoint without touching /app/config.php.
 * Version: 1.2.0
 * Author: Solutionium
 */

if (!defined('ABSPATH')) {
    exit;
}

final class Woo_App_Config_Manager {
    const OPTION_KEY = 'woo_app_config_json';
    const REVIEW_SETTINGS_OPTION_KEY = 'woo_app_review_settings';
    const REVIEW_CRITERIA_OPTION_KEY = 'woo_app_review_criteria_rows';
    const QUERY_VAR = 'woo_app_config_test';
    const REVIEW_QUERY_VAR = 'woo_app_review_criteria_test';
    const REST_NS = 'woo-app-config/v1';
    const FAST_DIR_RELATIVE = 'app';
    const FAST_JSON_FILE = 'config-test.json';
    const FAST_PHP_FILE = 'config-test.php';
    const REVIEW_FAST_JSON_FILE = 'review-criteria-test.json';
    const REVIEW_FAST_PHP_FILE = 'review-criteria-test.php';

    public static function init() {
        add_action('admin_menu', [__CLASS__, 'register_admin_menu']);
        add_action('admin_init', [__CLASS__, 'register_setting']);
        add_action('admin_enqueue_scripts', [__CLASS__, 'enqueue_admin_assets']);

        add_action('update_option_' . self::OPTION_KEY, [__CLASS__, 'handle_option_updated'], 10, 3);
        add_action('update_option_' . self::REVIEW_SETTINGS_OPTION_KEY, [__CLASS__, 'handle_option_updated'], 10, 3);
        add_action('update_option_' . self::REVIEW_CRITERIA_OPTION_KEY, [__CLASS__, 'handle_option_updated'], 10, 3);

        add_action('rest_api_init', [__CLASS__, 'register_rest_routes']);
        add_action('woocommerce_rest_insert_product_review', [__CLASS__, 'handle_wc_rest_review_insert'], 10, 3);
        add_filter('woocommerce_rest_prepare_product_review_object', [__CLASS__, 'handle_wc_rest_prepare_product_review'], 10, 3);

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
        self::write_review_criteria_cache_file();
        self::write_review_criteria_fast_files();
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
        unset($decoded['review_criteria']);

        add_settings_error(self::OPTION_KEY, 'saved', 'Configuration saved successfully.', 'updated');

        return wp_json_encode($decoded, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    }

    public static function handle_option_updated($old_value, $value, $option) {
        self::write_cache_file();
        self::write_fast_endpoint_files();
        self::write_review_criteria_cache_file();
        self::write_review_criteria_fast_files();
    }

    public static function register_rest_routes() {
        register_rest_route(self::REST_NS, '/config', [
            'methods' => 'GET',
            'permission_callback' => '__return_true',
            'callback' => function () {
                return rest_ensure_response(self::get_config_array());
            },
        ]);

        register_rest_route(self::REST_NS, '/product_reviews', [
            'methods' => 'GET',
            'permission_callback' => '__return_true',
            'callback' => [__CLASS__, 'rest_get_product_reviews'],
        ]);

        register_rest_route(self::REST_NS, '/submit_review', [
            'methods' => 'POST',
            'permission_callback' => '__return_true',
            'callback' => [__CLASS__, 'rest_submit_review'],
        ]);

        register_rest_route(self::REST_NS, '/review_criteria', [
            'methods' => 'GET',
            'permission_callback' => '__return_true',
            'callback' => [__CLASS__, 'rest_get_review_criteria'],
        ]);
    }

    public static function rest_get_product_reviews($request) {
        $product_id = absint($request->get_param('product_id'));
        if ($product_id <= 0) {
            $product_id = absint($request->get_param('product'));
        }

        $page = max(1, absint($request->get_param('page')));
        $per_page = absint($request->get_param('per_page'));
        if ($per_page <= 0) {
            $per_page = 10;
        }
        $per_page = min($per_page, 100);

        $status = sanitize_key((string) $request->get_param('status'));
        if ($status === '') {
            $status = 'approve';
        }
        if ($status === 'approved') {
            $status = 'approve';
        }

        $args = [
            // Include both WooCommerce "review" comments and integrations that save as standard comments.
            'type__in' => ['review', 'comment', ''],
            'parent' => 0,
            'status' => $status,
            'number' => $per_page,
            'offset' => ($page - 1) * $per_page,
            'orderby' => 'comment_date_gmt',
            'order' => 'DESC',
            'post_type' => 'product',
            'post_status' => 'publish',
        ];

        if ($product_id > 0) {
            $args['post_id'] = $product_id;
        }

        $comments = get_comments($args);
        if (!is_array($comments)) {
            $comments = [];
        }

        $items = [];
        foreach ($comments as $comment) {
            $items[] = self::normalize_review_comment($comment);
        }

        return rest_ensure_response($items);
    }

    public static function rest_submit_review($request) {
        $params = $request->get_json_params();
        if (!is_array($params)) {
            $params = $request->get_params();
        }

        $product_id = self::i(self::g($params, ['product_id'], 0));
        if ($product_id <= 0) {
            return new WP_Error('woo_app_config_invalid_product', 'product_id is required.', ['status' => 400]);
        }

        $product = function_exists('wc_get_product') ? wc_get_product($product_id) : null;
        if (!$product) {
            return new WP_Error('woo_app_config_invalid_product', 'Product not found.', ['status' => 404]);
        }

        $current_user = wp_get_current_user();
        $reviewer = sanitize_text_field((string) self::g($params, ['reviewer'], ''));
        if ($reviewer === '' && $current_user && $current_user->exists()) {
            $reviewer = $current_user->display_name ? $current_user->display_name : $current_user->user_login;
        }

        $reviewer_email = sanitize_email((string) self::g($params, ['reviewer_email'], ''));
        if ($reviewer_email === '' && $current_user && $current_user->exists()) {
            $reviewer_email = sanitize_email((string) $current_user->user_email);
        }
        if ($reviewer_email === '' || !is_email($reviewer_email)) {
            return new WP_Error('woo_app_config_invalid_email', 'A valid reviewer_email is required.', ['status' => 400]);
        }

        $review_text = trim((string) self::g($params, ['review'], ''));
        if ($review_text === '') {
            return new WP_Error('woo_app_config_invalid_review', 'Review content cannot be empty.', ['status' => 400]);
        }

        $status_raw = sanitize_key((string) self::g($params, ['status'], 'hold'));
        $criteria_ratings = self::normalize_criteria_ratings(self::g($params, ['criteria_ratings'], []));
        $rating = self::i(self::g($params, ['rating'], 0));
        $rating = max(1, min(5, $rating));

        if (self::is_yith_mode_enabled()) {
            $yithReview = self::create_yith_review(
                $product,
                $reviewer,
                $reviewer_email,
                $review_text,
                $rating,
                $criteria_ratings,
                $status_raw,
            );

            if ($yithReview && method_exists($yithReview, 'get_comment_id')) {
                $comment_id = (int) $yithReview->get_comment_id();
                if ($comment_id > 0) {
                    $comment = get_comment($comment_id);
                    if ($comment) {
                        return rest_ensure_response(self::normalize_review_comment($comment));
                    }
                }
            }
        }

        $comment_approved = in_array($status_raw, ['1', 'approve', 'approved', 'publish'], true) ? 1 : 0;

        $comment_data = [
            'comment_post_ID' => $product_id,
            'comment_content' => wp_kses_post($review_text),
            'comment_type' => 'review',
            'comment_parent' => 0,
            'comment_approved' => $comment_approved,
            'comment_author' => $reviewer,
            'comment_author_email' => $reviewer_email,
            'user_id' => get_current_user_id(),
        ];

        $comment_id = wp_new_comment(wp_slash($comment_data), true);
        if (is_wp_error($comment_id)) {
            return $comment_id;
        }
        $comment_id = (int) $comment_id;

        update_comment_meta($comment_id, 'rating', $rating);
        self::save_comment_criteria_ratings($comment_id, $criteria_ratings);

        if (function_exists('wc_delete_product_transients')) {
            wc_delete_product_transients($product_id);
        }
        if (class_exists('WC_Comments') && method_exists('WC_Comments', 'clear_transients')) {
            WC_Comments::clear_transients();
        }

        $comment = get_comment($comment_id);
        if (!$comment) {
            return new WP_Error('woo_app_config_review_not_found', 'Review created but could not be loaded.', ['status' => 500]);
        }

        return rest_ensure_response(self::normalize_review_comment($comment));
    }

    public static function rest_get_review_criteria($request) {
        $product_id = absint($request->get_param('product_id'));
        $category_ids = self::extract_int_list($request->get_param('category_ids'));
        $language = self::resolve_request_language($request);

        if (empty($category_ids) && $product_id > 0) {
            $category_ids = wp_get_post_terms($product_id, 'product_cat', ['fields' => 'ids']);
            if (!is_array($category_ids)) {
                $category_ids = [];
            }
            $category_ids = array_map('intval', $category_ids);
        }

        $source = 'none';
        $criteria = self::resolve_review_criteria($product_id, $category_ids, $source, $language);
        $provider = self::is_yith_mode_enabled() ? 'yith' : 'native';

        return rest_ensure_response([
            'success' => true,
            'data' => [
                'criteria' => array_values($criteria),
                'source' => $source,
                'provider' => $provider,
                'language' => $language,
            ],
        ]);
    }

    public static function handle_wc_rest_review_insert($comment, $request, $creating) {
        $comment_id = 0;
        if (is_object($comment) && isset($comment->comment_ID)) {
            $comment_id = (int) $comment->comment_ID;
        } elseif (is_numeric($comment)) {
            $comment_id = (int) $comment;
        }

        if ($comment_id <= 0 || !is_object($request) || !method_exists($request, 'get_param')) {
            return;
        }

        $criteria_ratings = self::normalize_criteria_ratings($request->get_param('criteria_ratings'));
        if (!empty($criteria_ratings)) {
            self::save_comment_criteria_ratings($comment_id, $criteria_ratings);
        }
    }

    public static function handle_wc_rest_prepare_product_review($response, $comment, $request) {
        if (!is_object($response) || !method_exists($response, 'get_data') || !method_exists($response, 'set_data')) {
            return $response;
        }

        $comment_id = 0;
        if (is_object($comment) && isset($comment->comment_ID)) {
            $comment_id = (int) $comment->comment_ID;
        } elseif (is_numeric($comment)) {
            $comment_id = (int) $comment;
        }

        $data = $response->get_data();
        if (!is_array($data)) {
            return $response;
        }

        $data['criteria_ratings'] = $comment_id > 0 ? self::read_comment_criteria_ratings($comment_id) : [];
        $response->set_data($data);

        return $response;
    }

    public static function register_query_vars($vars) {
        $vars[] = self::QUERY_VAR;
        $vars[] = self::REVIEW_QUERY_VAR;
        return $vars;
    }

    public static function register_rewrite_rules() {
        add_rewrite_rule('^app/config-test\.php$', 'index.php?' . self::QUERY_VAR . '=1', 'top');
        add_rewrite_rule('^app/review-criteria-test\.php$', 'index.php?' . self::REVIEW_QUERY_VAR . '=1', 'top');
    }

    public static function handle_test_endpoint() {
        if ((int) get_query_var(self::QUERY_VAR, 0) === 1) {
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

        if ((int) get_query_var(self::REVIEW_QUERY_VAR, 0) === 1) {
            $payload = self::get_review_criteria_cached_or_live_json();
            if ($payload === null) {
                $payload = wp_json_encode(self::build_review_criteria_cache_payload(), JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            }

            nocache_headers();
            status_header(200);
            header('Content-Type: application/json; charset=utf-8');
            echo $payload;
            exit;
        }
    }

    public static function render_admin_page() {
        if (!current_user_can('manage_options')) {
            wp_die('You do not have permission to access this page.');
        }

        self::maybe_seed_default_config();

        if (isset($_POST['wam_regenerate']) && check_admin_referer('wam_regenerate_cache', 'wam_nonce')) {
            self::write_cache_file();
            self::write_fast_endpoint_files();
            self::write_review_criteria_cache_file();
            self::write_review_criteria_fast_files();
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
        $reviewSettings = self::get_review_settings();
        $reviewCriteriaRows = self::get_review_criteria_rows();
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
        $review_fast_endpoint_url = home_url('/app/review-criteria-test.php');
        $review_fast_json_url = home_url('/app/review-criteria-test.json');
        $rest_url = rest_url(self::REST_NS . '/config');
        $review_criteria_rest_url = rest_url(self::REST_NS . '/review_criteria');
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
                            <?php self::render_gui_form($config, $reviewSettings, $reviewCriteriaRows, $brands, $attrs, $attr_terms); ?>
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
                            <li>Review criteria fast endpoint:<br><a href="<?php echo esc_url($review_fast_endpoint_url); ?>" target="_blank" rel="noopener noreferrer" class="wam-mono"><?php echo esc_html($review_fast_endpoint_url); ?></a></li>
                            <li>Review criteria fast JSON:<br><a href="<?php echo esc_url($review_fast_json_url); ?>" target="_blank" rel="noopener noreferrer" class="wam-mono"><?php echo esc_html($review_fast_json_url); ?></a></li>
                            <li>Review criteria REST endpoint:<br><a href="<?php echo esc_url($review_criteria_rest_url); ?>" target="_blank" rel="noopener noreferrer" class="wam-mono"><?php echo esc_html($review_criteria_rest_url); ?></a></li>
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

    private static function render_gui_form($config, $reviewSettings, $reviewCriteriaRows, $brands, $attrs, $attr_terms) {
        $search_tabs = isset($config['search_tab']) && is_array($config['search_tab']) ? $config['search_tab'] : [];
        $stories = isset($config['stories']) && is_array($config['stories']) ? $config['stories'] : [];
        $banners = isset($config['home_banner']) && is_array($config['home_banner']) ? $config['home_banner'] : [];
        $images = isset($config['images']) && is_array($config['images']) ? $config['images'] : [];
        $review_criteria = is_array($reviewCriteriaRows) ? $reviewCriteriaRows : [];
        $shipping_options = self::get_shipping_method_options();
        $category_options = self::get_product_category_options();
        $yithActive = self::is_yith_reviews_available();
        $yithEnabled = !empty($reviewSettings['enable_yith']) && $yithActive;

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
                    <div class="wam-field" style="grid-column:1/-1;">
                        <label style="display:flex;align-items:center;gap:8px;">
                            <input
                                type="checkbox"
                                name="review_cfg[enable_yith]"
                                value="1"
                                <?php checked($yithEnabled); ?>
                                <?php disabled(!$yithActive); ?>
                            >
                            Enable YITH Advanced Reviews integration
                        </label>
                        <small style="color:#667085;">
                            <?php echo $yithActive ? 'YITH plugin detected. Review boxes and criteria will be used when enabled.' : 'YITH plugin is not active on this site. Activate it to enable this option.'; ?>
                        </small>
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
        $reviewCfg = isset($post['review_cfg']) && is_array($post['review_cfg']) ? $post['review_cfg'] : [];
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
        $manualReviewCriteriaRows = self::sanitize_review_criteria_rows(self::g($cfg, ['review_criteria'], []));
        update_option(self::REVIEW_CRITERIA_OPTION_KEY, $manualReviewCriteriaRows, false);

        $yithEnabled = self::b(self::g($reviewCfg, ['enable_yith'], false)) && self::is_yith_reviews_available();
        $reviewSettings = [
            'enable_yith' => $yithEnabled,
        ];
        update_option(self::REVIEW_SETTINGS_OPTION_KEY, $reviewSettings, false);

        // Keep review provider and endpoint metadata in app config, while criteria data stays in dedicated cache.
        $existing['review_provider'] = $yithEnabled ? 'yith' : 'native';
        $existing['review_criteria_endpoint'] = 'wp-json/woo-app-config/v1/review_criteria';
        unset($existing['review_criteria']);

        self::ensure_base_keys($existing);

        $json = wp_json_encode($existing, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if (!$json) {
            return false;
        }

        $ok = update_option(self::OPTION_KEY, $json, false);
        self::write_cache_file();
        self::write_review_criteria_cache_file();
        self::write_review_criteria_fast_files();

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

    private static function normalize_review_comment($comment) {
        $comment_obj = is_object($comment) ? $comment : get_comment((int) $comment);
        if (!$comment_obj || !isset($comment_obj->comment_ID)) {
            return [
                'id' => 0,
                'date_created' => '',
                'date_created_gmt' => '',
                'product_id' => 0,
                'product_name' => '',
                'product_permalink' => '',
                'status' => 'hold',
                'reviewer' => '',
                'reviewer_email' => '',
                'review' => '',
                'rating' => 0,
                'verified' => false,
                'criteria_ratings' => [],
                'children' => [],
            ];
        }

        $comment_id = (int) $comment_obj->comment_ID;
        $product_id = (int) $comment_obj->comment_post_ID;
        $approved = (string) $comment_obj->comment_approved;
        $status = $approved === '1' ? 'approved' : ($approved === 'spam' ? 'spam' : 'hold');

        $gmt_raw = (string) $comment_obj->comment_date_gmt;
        $gmt_ts = $gmt_raw !== '' ? strtotime($gmt_raw . ' UTC') : false;
        $date_created_gmt = $gmt_ts ? gmdate('Y-m-d\TH:i:s\Z', $gmt_ts) : '';
        $date_created = $gmt_raw !== '' ? get_date_from_gmt($gmt_raw, 'c') : '';

        $verified = false;
        if (function_exists('wc_review_is_from_verified_owner')) {
            $verified = (bool) wc_review_is_from_verified_owner($comment_id);
        }
        $badges = self::resolve_review_badges($comment_id, $verified);

        return [
            'id' => $comment_id,
            'date_created' => $date_created,
            'date_created_gmt' => $date_created_gmt,
            'product_id' => $product_id,
            'product_name' => $product_id > 0 ? html_entity_decode((string) get_the_title($product_id), ENT_QUOTES, 'UTF-8') : '',
            'product_permalink' => $product_id > 0 ? (string) get_permalink($product_id) : '',
            'status' => $status,
            'reviewer' => (string) $comment_obj->comment_author,
            'reviewer_email' => (string) $comment_obj->comment_author_email,
            'review' => (string) $comment_obj->comment_content,
            'rating' => (int) get_comment_meta($comment_id, 'rating', true),
            'verified' => !empty($badges['verified']),
            'verified_buyer' => !empty($badges['verified_buyer']),
            'featured' => !empty($badges['featured']),
            'helpful' => !empty($badges['helpful']),
            'helpful_votes' => (int) self::i(self::g($badges, ['helpful_votes'], 0)),
            'criteria_ratings' => self::read_comment_criteria_ratings($comment_id),
            'children' => self::get_review_children($comment_id),
        ];
    }

    private static function resolve_review_badges($comment_id, $verifiedFromWoo = false) {
        $comment_id = (int) $comment_id;
        static $cache = [];
        if ($comment_id > 0 && isset($cache[$comment_id])) {
            return $cache[$comment_id];
        }

        $verifiedBuyer = (bool) $verifiedFromWoo;
        $featured = false;
        $helpful = false;
        $helpfulVotes = 0;

        $yithReview = self::get_yith_review_by_comment_id($comment_id);
        if ($yithReview && is_object($yithReview)) {
            if (method_exists($yithReview, 'get_featured')) {
                $featured = strtolower((string) $yithReview->get_featured()) === 'yes';
            }
            if (method_exists($yithReview, 'get_helpful')) {
                $helpful = strtolower((string) $yithReview->get_helpful()) === 'yes';
            }
            if (method_exists($yithReview, 'get_upvotes_count')) {
                $helpfulVotes = max(0, (int) $yithReview->get_upvotes_count());
            }
            if (method_exists($yithReview, 'get_verified_owner')) {
                $verifiedBuyer = $verifiedBuyer || strtolower((string) $yithReview->get_verified_owner()) === 'yes';
            }
        }

        $resolved = [
            'verified' => $verifiedBuyer,
            'verified_buyer' => $verifiedBuyer,
            'featured' => $featured,
            'helpful' => $helpful || $helpfulVotes > 0,
            'helpful_votes' => $helpfulVotes,
        ];

        if ($comment_id > 0) {
            $cache[$comment_id] = $resolved;
        }

        return $resolved;
    }

    private static function get_yith_review_by_comment_id($comment_id) {
        $comment_id = (int) $comment_id;
        if ($comment_id <= 0 || !self::is_yith_reviews_available() || !function_exists('yith_ywar_get_reviews')) {
            return null;
        }

        static $cache = [];
        if (array_key_exists($comment_id, $cache)) {
            return $cache[$comment_id];
        }

        try {
            $reviews = yith_ywar_get_reviews([
                'posts_per_page' => 1,
                'post_status' => ['any', 'trash'],
                'meta_query' => [
                    [
                        'key' => '_ywar_comment_id',
                        'value' => $comment_id,
                        'compare' => '=',
                    ],
                ],
            ]);

            if (!is_array($reviews) || empty($reviews)) {
                $cache[$comment_id] = null;
                return null;
            }

            $review = reset($reviews);
            $cache[$comment_id] = is_object($review) ? $review : null;
            return $cache[$comment_id];
        } catch (\Throwable $e) {
            $cache[$comment_id] = null;
            return null;
        }
    }

    private static function get_review_children($comment_id) {
        $children = get_comments([
            'parent' => (int) $comment_id,
            'status' => 'approve',
            'orderby' => 'comment_date_gmt',
            'order' => 'ASC',
            'number' => 100,
        ]);

        if (!is_array($children) || empty($children)) {
            return [];
        }

        $items = [];
        foreach ($children as $child) {
            if (!is_object($child) || !isset($child->comment_ID)) {
                continue;
            }
            $items[] = [
                'id' => (string) $child->comment_ID,
                'author' => (string) $child->comment_author,
                'content' => (string) $child->comment_content,
                'date' => (string) get_date_from_gmt((string) $child->comment_date_gmt, 'c'),
                'avatar' => (string) get_avatar_url((int) $child->user_id, ['size' => 96]),
            ];
        }

        return $items;
    }

    private static function normalize_criteria_ratings($raw) {
        if (is_string($raw)) {
            $decoded = json_decode($raw, true);
            if (json_last_error() === JSON_ERROR_NONE) {
                $raw = $decoded;
            } else {
                $raw = [];
            }
        } elseif (is_object($raw)) {
            $raw = (array) $raw;
        }

        if (!is_array($raw)) {
            return [];
        }

        $rows = $raw;
        if (isset($raw['label']) && isset($raw['value'])) {
            $rows = [$raw];
        }

        $normalized = [];
        foreach ($rows as $row) {
            if (is_object($row)) {
                $row = (array) $row;
            }
            if (!is_array($row)) {
                continue;
            }

            $label = sanitize_text_field((string) self::g($row, ['label'], self::g($row, ['name'], '')));
            $value = self::i(self::g($row, ['value'], 0));
            $value = max(1, min(5, $value));
            if ($label === '' || $value <= 0) {
                continue;
            }

            $normalized[] = [
                'label' => $label,
                'value' => $value,
            ];
        }

        $seen = [];
        $unique = [];
        foreach ($normalized as $item) {
            $key = strtolower($item['label']);
            if (isset($seen[$key])) {
                continue;
            }
            $seen[$key] = true;
            $unique[] = $item;
        }

        return $unique;
    }

    private static function save_comment_criteria_ratings($comment_id, $criteria_ratings) {
        $normalized = self::normalize_criteria_ratings($criteria_ratings);
        update_comment_meta($comment_id, 'criteria_ratings', $normalized);
        update_comment_meta($comment_id, '_woo_app_criteria_ratings', $normalized);

        $yith_map = [];
        foreach ($normalized as $row) {
            $label = isset($row['label']) ? (string) $row['label'] : '';
            $value = isset($row['value']) ? (int) $row['value'] : 0;
            if ($label === '' || $value <= 0) {
                continue;
            }

            $slug = sanitize_title($label);
            if ($slug === '') {
                continue;
            }

            $yith_map[$slug] = $value;
            update_comment_meta($comment_id, 'ywar_criteria_' . $slug, $value);
            update_comment_meta($comment_id, '_ywar_criteria_' . $slug, $value);
        }

        if (!empty($yith_map)) {
            update_comment_meta($comment_id, 'ywar_criteria_ratings', $yith_map);
            update_comment_meta($comment_id, '_ywar_criteria_ratings', $yith_map);
        }
    }

    private static function read_comment_criteria_ratings($comment_id) {
        $fromYith = self::read_yith_multi_rating_for_comment($comment_id);
        if (!empty($fromYith)) {
            return $fromYith;
        }

        $keys = ['criteria_ratings', '_woo_app_criteria_ratings', 'ywar_criteria_ratings', '_ywar_criteria_ratings'];
        foreach ($keys as $key) {
            $raw = get_comment_meta($comment_id, $key, true);
            $normalized = self::normalize_criteria_ratings($raw);
            if (!empty($normalized)) {
                return $normalized;
            }
        }

        $meta = get_comment_meta($comment_id);
        if (!is_array($meta) || empty($meta)) {
            return [];
        }

        $rows = [];
        foreach ($meta as $key => $values) {
            $v = is_array($values) ? reset($values) : $values;
            if (!is_scalar($v) || !is_numeric($v)) {
                continue;
            }

            $k = strtolower((string) $key);
            if (strpos($k, 'criteria') === false && strpos($k, 'ywar') === false) {
                continue;
            }

            $label = preg_replace('/^_+/', '', (string) $key);
            $label = preg_replace('/^ywar_criteria_/', '', $label);
            $label = preg_replace('/^criteria_/', '', $label);
            $label = str_replace(['_', '-'], ' ', $label);
            $label = trim($label);
            if ($label === '') {
                continue;
            }

            $rows[] = [
                'label' => ucwords($label),
                'value' => max(1, min(5, (int) $v)),
            ];
        }

        return self::normalize_criteria_ratings($rows);
    }

    private static function read_yith_multi_rating_for_comment($comment_id) {
        if (!self::is_yith_reviews_available() || !function_exists('yith_ywar_get_reviews')) {
            return [];
        }

        $reviews = yith_ywar_get_reviews([
            'posts_per_page' => 1,
            'meta_query' => [
                [
                    'key' => '_ywar_comment_id',
                    'value' => (int) $comment_id,
                    'compare' => '=',
                ],
            ],
        ]);

        if (!is_array($reviews) || empty($reviews)) {
            return [];
        }

        $review = reset($reviews);
        if (!is_object($review) || !method_exists($review, 'get_multi_rating')) {
            return [];
        }

        $multiRating = (array) $review->get_multi_rating();
        if (empty($multiRating)) {
            return [];
        }

        $criteriaTax = defined('YITH_YWAR_Post_Types::CRITERIA_TAX') ? YITH_YWAR_Post_Types::CRITERIA_TAX : 'ywar_review_criteria';
        $normalized = [];
        foreach ($multiRating as $criterionId => $value) {
            $criterionId = (int) $criterionId;
            $value = max(1, min(5, (int) $value));
            if ($criterionId <= 0 || $value <= 0) {
                continue;
            }
            $term = get_term($criterionId, $criteriaTax);
            $label = is_object($term) && !is_wp_error($term) ? (string) $term->name : (string) $criterionId;
            $normalized[] = [
                'label' => $label,
                'value' => $value,
            ];
        }

        return self::normalize_criteria_ratings($normalized);
    }

    private static function create_yith_review($product, $reviewer, $reviewerEmail, $reviewText, $rating, $criteriaRatings, $statusRaw) {
        if (!self::is_yith_reviews_available()) {
            return null;
        }
        if (!is_object($product) || !method_exists($product, 'get_id')) {
            return null;
        }

        try {
            $productId = (int) $product->get_id();
            $reviewBox = function_exists('yith_ywar_get_current_review_box') ? yith_ywar_get_current_review_box($product) : null;
            $criteriaIds = (is_object($reviewBox) && method_exists($reviewBox, 'get_multi_criteria'))
                ? array_map('intval', (array) $reviewBox->get_multi_criteria())
                : [];

            $criteriaTax = defined('YITH_YWAR_Post_Types::CRITERIA_TAX') ? YITH_YWAR_Post_Types::CRITERIA_TAX : 'ywar_review_criteria';
            $criteriaTermByNormalizedName = [];
            foreach ($criteriaIds as $criterionId) {
                if ($criterionId <= 0) {
                    continue;
                }
                $term = get_term($criterionId, $criteriaTax);
                if (!is_object($term) || is_wp_error($term)) {
                    continue;
                }
                $key = strtolower(trim((string) $term->name));
                if ($key !== '') {
                    $criteriaTermByNormalizedName[$key] = (int) $term->term_id;
                }
            }

            $multiRating = [];
            $pendingUnmappedValues = [];
            foreach ($criteriaRatings as $item) {
                if (!is_array($item)) {
                    continue;
                }
                $label = strtolower(trim((string) self::g($item, ['label'], '')));
                $value = max(1, min(5, self::i(self::g($item, ['value'], 0))));
                if ($value <= 0) {
                    continue;
                }
                if ($label !== '' && isset($criteriaTermByNormalizedName[$label])) {
                    $multiRating[(int) $criteriaTermByNormalizedName[$label]] = $value;
                } else {
                    $pendingUnmappedValues[] = $value;
                }
            }

            if (!empty($pendingUnmappedValues)) {
                foreach ($criteriaIds as $criterionId) {
                    if (empty($pendingUnmappedValues)) {
                        break;
                    }
                    if (isset($multiRating[$criterionId])) {
                        continue;
                    }
                    $multiRating[$criterionId] = array_shift($pendingUnmappedValues);
                }
            }

            if (!empty($multiRating)) {
                if (function_exists('yith_ywar_calculate_avg_rating')) {
                    $rating = (int) yith_ywar_calculate_avg_rating($multiRating);
                } else {
                    $rating = (int) round(array_sum($multiRating) / max(1, count($multiRating)));
                }
            }

            $autoApproval = function_exists('yith_ywar_get_option') ? ('yes' === yith_ywar_get_option('ywar_review_autoapprove')) : false;
            $approvedByStatus = in_array((string) $statusRaw, ['1', 'approve', 'approved', 'publish'], true);
            $status = ($autoApproval || $approvedByStatus) ? 'approved' : 'pending';

            $review = new YITH_YWAR_Review();
            $fields = [
                'title' => substr(wp_strip_all_tags($reviewText), 0, 80),
                'content' => $reviewText,
                'rating' => max(1, min(5, (int) $rating)),
                'multi_rating' => $multiRating,
                'product_id' => $productId,
                'status' => $status,
                'review_user_id' => get_current_user_id(),
                'review_author' => $reviewer,
                'review_author_email' => $reviewerEmail,
                'review_author_IP' => class_exists('WC_Geolocation') ? WC_Geolocation::get_external_ip_address() : '',
                'review_author_country' => class_exists('WC_Geolocation') ? ((array) WC_Geolocation::geolocate_ip('', true))['country'] ?? '' : '',
                'thumb_ids' => [],
                'guest_cookie' => '',
            ];
            foreach ($fields as $field => $value) {
                $setter = "set_$field";
                if (method_exists($review, $setter)) {
                    $review->{$setter}($value);
                }
            }

            $review->save();
            return $review;
        } catch (\Throwable $e) {
            return null;
        }
    }

    private static function resolve_review_criteria($product_id, $category_ids, &$source = 'none', $language = 'en') {
        $snapshot = self::get_review_criteria_cached_payload_array();
        $useYith = self::is_yith_mode_enabled() && !empty(self::g($snapshot, ['review_boxes'], []));

        if ($useYith) {
            $criteria = self::resolve_yith_criteria_from_cache($snapshot, $product_id, $category_ids);
            if (!empty($criteria)) {
                $source = 'yith_review_box';
                return $criteria;
            }
        }

        $manualRows = self::g($snapshot, ['manual_review_criteria'], []);
        if (!is_array($manualRows)) {
            $manualRows = [];
        }

        $criteria = self::pick_config_review_criteria($manualRows, $category_ids, $language);
        if (!empty($criteria)) {
            $source = 'manual_review_criteria';
            return $criteria;
        }

        // Fallback to direct YITH resolution only when cache cannot provide data.
        $criteria = self::resolve_review_criteria_from_yith($product_id, $category_ids);
        if (!empty($criteria)) {
            $source = 'yith_review_box_runtime';
            return $criteria;
        }

        $source = 'none';
        return [];
    }

    private static function resolve_yith_criteria_from_cache($snapshot, $product_id, $category_ids) {
        $boxes = self::g($snapshot, ['review_boxes'], []);
        if (!is_array($boxes) || empty($boxes)) {
            return [];
        }

        $criteriaTerms = self::g($snapshot, ['criteria_terms'], []);
        $criteriaNameById = [];
        if (is_array($criteriaTerms)) {
            foreach ($criteriaTerms as $term) {
                if (!is_array($term)) {
                    continue;
                }
                $id = self::i(self::g($term, ['id'], 0));
                $name = self::s(self::g($term, ['name'], ''));
                if ($id > 0 && $name !== '') {
                    $criteriaNameById[$id] = $name;
                }
            }
        }

        $catIds = $category_ids;
        $tagIds = [];
        $isVirtual = false;
        if ($product_id > 0) {
            $product = function_exists('wc_get_product') ? wc_get_product($product_id) : null;
            if ($product) {
                if (empty($catIds) && method_exists($product, 'get_category_ids')) {
                    $catIds = array_map('intval', (array) $product->get_category_ids());
                }
                if (method_exists($product, 'get_tag_ids')) {
                    $tagIds = array_map('intval', (array) $product->get_tag_ids());
                }
                if (method_exists($product, 'is_virtual')) {
                    $isVirtual = (bool) $product->is_virtual();
                }
            }
        }

        usort($boxes, function ($a, $b) {
            return self::i(self::g($b, ['id'], 0)) <=> self::i(self::g($a, ['id'], 0));
        });

        $selected = null;
        foreach ($boxes as $box) {
            if (!is_array($box)) {
                continue;
            }
            if (!self::g($box, ['active'], false)) {
                continue;
            }

            $showOn = strtolower((string) self::g($box, ['show_on'], ''));
            $matched = false;
            if ($showOn === 'virtual') {
                $matched = $isVirtual;
            } elseif ($showOn === 'products') {
                $matched = in_array((int) $product_id, array_map('intval', (array) self::g($box, ['product_ids'], [])), true);
            } elseif ($showOn === 'categories') {
                $boxCatIds = array_map('intval', (array) self::g($box, ['category_ids'], []));
                $matched = !empty(array_intersect($catIds, $boxCatIds));
            } elseif ($showOn === 'tags') {
                $boxTagIds = array_map('intval', (array) self::g($box, ['tag_ids'], []));
                $matched = !empty(array_intersect($tagIds, $boxTagIds));
            } elseif ($showOn === 'all') {
                $matched = true;
            }

            if ($matched) {
                $selected = $box;
                break;
            }
        }

        if (!$selected) {
            $defaultId = self::i(self::g($snapshot, ['default_box_id'], 0));
            if ($defaultId > 0) {
                foreach ($boxes as $box) {
                    if (self::i(self::g($box, ['id'], 0)) === $defaultId) {
                        $selected = $box;
                        break;
                    }
                }
            }
        }

        if (!$selected) {
            return [];
        }

        $criteriaIds = array_map('intval', (array) self::g($selected, ['multi_criteria'], []));
        $criteria = [];
        foreach ($criteriaIds as $criteriaId) {
            if ($criteriaId > 0 && isset($criteriaNameById[$criteriaId])) {
                $criteria[] = $criteriaNameById[$criteriaId];
            }
        }

        if (!empty($criteria)) {
            return $criteria;
        }

        return self::extract_criteria_strings(self::g($selected, ['multi_criteria'], []));
    }

    private static function resolve_review_criteria_from_yith($product_id, $category_ids) {
        if ($product_id > 0 && function_exists('wc_get_product') && function_exists('yith_ywar_get_current_review_box')) {
            try {
                $product = wc_get_product((int) $product_id);
                if ($product) {
                    $reviewBox = yith_ywar_get_current_review_box($product);
                    if ($reviewBox && is_object($reviewBox)) {
                        $multiEnabled = method_exists($reviewBox, 'get_enable_multi_criteria')
                            ? strtolower((string) $reviewBox->get_enable_multi_criteria()) === 'yes'
                            : false;
                        if ($multiEnabled && method_exists($reviewBox, 'get_multi_criteria')) {
                            $criteriaIds = array_map('intval', (array) $reviewBox->get_multi_criteria());
                            $criteriaTax = defined('YITH_YWAR_Post_Types::CRITERIA_TAX') ? YITH_YWAR_Post_Types::CRITERIA_TAX : 'ywar_review_criteria';
                            $criteria = [];
                            foreach ($criteriaIds as $criteriaId) {
                                if ($criteriaId <= 0) {
                                    continue;
                                }
                                $term = get_term($criteriaId, $criteriaTax);
                                if ($term && !is_wp_error($term) && isset($term->name)) {
                                    $name = trim((string) $term->name);
                                    if ($name !== '') {
                                        $criteria[] = $name;
                                    }
                                }
                            }
                            if (!empty($criteria)) {
                                return array_values(array_unique($criteria));
                            }
                        }
                    }
                }
            } catch (\Throwable $e) {
                // Continue with fallback strategies.
            }
        }

        $filters = [
            'yith_wcar_review_criteria_for_product',
            'yith_ywar_review_criteria_for_product',
            'yith_wcar_review_box_criteria',
            'yith_ywar_review_box_criteria',
        ];
        foreach ($filters as $filter_name) {
            $result = apply_filters($filter_name, [], (int) $product_id, $category_ids);
            $criteria = self::extract_criteria_strings($result);
            if (!empty($criteria)) {
                return $criteria;
            }
        }

        $instances = [];
        if (function_exists('YITH_YWAR')) {
            $instances[] = YITH_YWAR();
        }
        if (function_exists('yith_advanced_reviews')) {
            $instances[] = yith_advanced_reviews();
        }

        $candidate_methods = [
            'get_review_box_criteria',
            'get_review_box_criteria_for_product',
            'get_review_criteria_for_product',
            'get_review_criteria',
        ];

        foreach ($instances as $instance) {
            if (!is_object($instance)) {
                continue;
            }
            foreach ($candidate_methods as $method) {
                if (!method_exists($instance, $method)) {
                    continue;
                }

                $attempts = [
                    [$product_id, $category_ids],
                    [$product_id],
                    [$category_ids],
                    [],
                ];

                foreach ($attempts as $args) {
                    try {
                        $result = call_user_func_array([$instance, $method], $args);
                    } catch (\Throwable $e) {
                        $result = null;
                    }
                    $criteria = self::extract_criteria_strings($result);
                    if (!empty($criteria)) {
                        return $criteria;
                    }
                }
            }
        }

        $option_keys = [
            'yith_ywar_review_boxes',
            'ywar_review_boxes',
            'yith_ywar_review_criteria',
            'ywar_review_criteria',
        ];

        foreach ($option_keys as $option_key) {
            $raw = get_option($option_key, null);
            if ($raw === null) {
                continue;
            }
            $criteria = self::extract_criteria_strings($raw);
            if (!empty($criteria)) {
                return $criteria;
            }
        }

        return [];
    }

    private static function pick_config_review_criteria($rows, $category_ids, $language = 'en') {
        if (!is_array($rows) || empty($rows)) {
            return [];
        }

        $cat_set = [];
        foreach ($category_ids as $cat_id) {
            $cat_set[(int) $cat_id] = true;
        }

        foreach ($rows as $row) {
            if (!is_array($row)) {
                continue;
            }
            $cat_id = self::i(self::g($row, ['cat_id'], 0));
            if (!empty($cat_set) && $cat_id > 0 && isset($cat_set[$cat_id])) {
                $criteria = self::select_row_criteria_for_language($row, $language);
                if (!empty($criteria)) {
                    return $criteria;
                }
            }
        }

        foreach ($rows as $row) {
            if (!is_array($row)) {
                continue;
            }
            $criteria = self::select_row_criteria_for_language($row, $language);
            if (!empty($criteria)) {
                return $criteria;
            }
        }

        return [];
    }

    private static function select_row_criteria_for_language($row, $language = 'en') {
        if (!is_array($row)) {
            return [];
        }

        $lang = strtolower((string) $language);
        if (strpos($lang, 'ar') === 0) {
            $order = ['criteria_ar', 'criteria', 'criteria_fa'];
        } elseif (strpos($lang, 'fa') === 0) {
            $order = ['criteria_fa', 'criteria', 'criteria_ar'];
        } else {
            $order = ['criteria', 'criteria_ar', 'criteria_fa'];
        }

        foreach ($order as $key) {
            $criteria = self::extract_criteria_strings(self::g($row, [$key], []));
            if (!empty($criteria)) {
                return $criteria;
            }
        }

        return [];
    }

    private static function extract_criteria_strings($raw) {
        if (is_string($raw)) {
            $value = trim($raw);
            return $value === '' ? [] : [$value];
        }

        if (is_object($raw)) {
            $raw = (array) $raw;
        }

        if (!is_array($raw)) {
            return [];
        }

        $list = [];

        if (isset($raw['criteria'])) {
            $list = array_merge($list, self::extract_criteria_strings($raw['criteria']));
        }
        if (isset($raw['label']) && is_string($raw['label'])) {
            $label = trim($raw['label']);
            if ($label !== '') {
                $list[] = $label;
            }
        }
        if (isset($raw['name']) && is_string($raw['name'])) {
            $name = trim($raw['name']);
            if ($name !== '') {
                $list[] = $name;
            }
        }

        foreach ($raw as $item) {
            if (is_array($item) || is_object($item)) {
                $list = array_merge($list, self::extract_criteria_strings($item));
                continue;
            }
            if (is_string($item)) {
                $value = trim($item);
                if ($value !== '') {
                    $list[] = $value;
                }
            }
        }

        $seen = [];
        $unique = [];
        foreach ($list as $value) {
            $key = strtolower($value);
            if (isset($seen[$key])) {
                continue;
            }
            $seen[$key] = true;
            $unique[] = $value;
        }

        return $unique;
    }

    private static function extract_int_list($raw) {
        if (is_string($raw)) {
            $raw = preg_split('/\s*,\s*/', trim($raw));
        } elseif (is_object($raw)) {
            $raw = (array) $raw;
        }

        if (!is_array($raw)) {
            return [];
        }

        $ids = [];
        foreach ($raw as $item) {
            $id = (int) $item;
            if ($id > 0) {
                $ids[$id] = $id;
            }
        }

        return array_values($ids);
    }

    private static function resolve_request_language($request = null) {
        $candidates = [];

        if (is_object($request) && method_exists($request, 'get_param')) {
            $candidates[] = (string) $request->get_param('lang');
            $candidates[] = (string) $request->get_param('locale');
        }

        if (function_exists('determine_locale')) {
            $candidates[] = (string) determine_locale();
        }
        if (function_exists('get_locale')) {
            $candidates[] = (string) get_locale();
        }

        foreach ($candidates as $candidate) {
            $candidate = trim((string) $candidate);
            if ($candidate === '') {
                continue;
            }

            $candidate = strtolower(str_replace('_', '-', $candidate));
            $code = substr($candidate, 0, 2);
            if (in_array($code, ['en', 'ar', 'fa'], true)) {
                return $code;
            }
        }

        return 'en';
    }

    private static function ensure_base_keys(&$decoded) {
        if (!isset($decoded['status'])) {
            $decoded['status'] = 'ok';
        }
        if (!isset($decoded['message'])) {
            $decoded['message'] = 'Config generated from WordPress admin.';
        }
        foreach (['stories', 'search_tab', 'home_banner', 'images', 'payment_discount', 'payment_force_enabled'] as $k) {
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
        if (!isset($decoded['review_provider'])) {
            $decoded['review_provider'] = self::is_yith_mode_enabled() ? 'yith' : 'native';
        }
        if (!isset($decoded['review_criteria_endpoint'])) {
            $decoded['review_criteria_endpoint'] = 'wp-json/woo-app-config/v1/review_criteria';
        }
    }

    private static function maybe_seed_default_config() {
        if (get_option(self::OPTION_KEY, null) === null) {
            add_option(self::OPTION_KEY, self::default_json());
        }
        if (get_option(self::REVIEW_SETTINGS_OPTION_KEY, null) === null) {
            add_option(self::REVIEW_SETTINGS_OPTION_KEY, ['enable_yith' => false], false);
        }
        if (get_option(self::REVIEW_CRITERIA_OPTION_KEY, null) === null) {
            add_option(self::REVIEW_CRITERIA_OPTION_KEY, [], false);
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
            'review_provider' => 'native',
            'review_criteria_endpoint' => 'wp-json/woo-app-config/v1/review_criteria',
            'app_version' => ['latest_version' => '1.0', 'min_required_version' => '1.0'],
            'contact' => ['call' => '', 'whatsapp' => '', 'instagram' => '', 'telegram' => '', 'email' => ''],
        ];

        return wp_json_encode($default, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    }

    private static function get_config_array() {
        $json = get_option(self::OPTION_KEY, self::default_json());
        $decoded = json_decode((string) $json, true);
        if (!is_array($decoded)) {
            $decoded = [];
        }
        self::ensure_base_keys($decoded);
        $decoded['review_provider'] = self::is_yith_mode_enabled() ? 'yith' : 'native';
        $decoded['review_criteria_endpoint'] = 'wp-json/woo-app-config/v1/review_criteria';
        return $decoded;
    }

    private static function is_valid_json($json) {
        json_decode((string) $json, true);
        return json_last_error() === JSON_ERROR_NONE;
    }

    private static function get_review_settings() {
        $settings = get_option(self::REVIEW_SETTINGS_OPTION_KEY, ['enable_yith' => false]);
        if (!is_array($settings)) {
            $settings = ['enable_yith' => false];
        }
        $settings['enable_yith'] = !empty($settings['enable_yith']) && self::is_yith_reviews_available();
        return $settings;
    }

    private static function get_review_criteria_rows() {
        $rows = get_option(self::REVIEW_CRITERIA_OPTION_KEY, []);
        if (is_array($rows) && !empty($rows)) {
            return $rows;
        }

        return self::legacy_review_criteria_rows_from_app_config();
    }

    private static function legacy_review_criteria_rows_from_app_config() {
        $json = get_option(self::OPTION_KEY, '');
        if (!is_string($json) || trim($json) === '') {
            return [];
        }

        $decoded = json_decode($json, true);
        if (!is_array($decoded)) {
            return [];
        }

        $legacyRows = self::g($decoded, ['review_criteria'], []);
        if (!is_array($legacyRows) || empty($legacyRows)) {
            return [];
        }

        $normalized = [];
        foreach ($legacyRows as $row) {
            if (!is_array($row)) {
                continue;
            }

            $catId = self::i(self::g($row, ['cat_id'], 0));
            $criteria = self::extract_criteria_strings(self::g($row, ['criteria'], []));
            $criteriaAr = self::extract_criteria_strings(self::g($row, ['criteria_ar'], []));
            $criteriaFa = self::extract_criteria_strings(self::g($row, ['criteria_fa'], []));

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
                $normalized[] = $item;
            }
        }

        return $normalized;
    }

    private static function sanitize_review_criteria_rows($reviewRows) {
        $output = [];
        if (!is_array($reviewRows)) {
            return $output;
        }

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
                $output[] = $item;
            }
        }

        return $output;
    }

    private static function is_yith_reviews_available() {
        return function_exists('yith_ywar_get_current_review_box')
            && function_exists('yith_ywar_get_review_boxes')
            && class_exists('YITH_YWAR_Review')
            && class_exists('YITH_YWAR_Post_Types');
    }

    private static function is_yith_mode_enabled() {
        $settings = self::get_review_settings();
        return !empty($settings['enable_yith']) && self::is_yith_reviews_available();
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

        $json = wp_json_encode(self::get_config_array(), JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if (!is_string($json) || $json === '') {
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

        $json = wp_json_encode(self::get_config_array(), JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if (!is_string($json) || $json === '') {
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

    private static function review_criteria_cache_file_path() {
        return trailingslashit(self::cache_dir_path()) . 'review-criteria.json';
    }

    private static function get_review_criteria_cached_or_live_json() {
        $path = self::review_criteria_cache_file_path();
        if (!file_exists($path)) {
            return null;
        }
        $contents = file_get_contents($path);
        return $contents !== false ? $contents : null;
    }

    private static function get_review_criteria_cached_payload_array() {
        $raw = self::get_review_criteria_cached_or_live_json();
        if (is_string($raw) && $raw !== '') {
            $decoded = json_decode($raw, true);
            if (is_array($decoded)) {
                return $decoded;
            }
        }
        $payload = self::build_review_criteria_cache_payload();
        self::write_review_criteria_cache_file_from_payload($payload);
        return $payload;
    }

    private static function write_review_criteria_cache_file() {
        return self::write_review_criteria_cache_file_from_payload(self::build_review_criteria_cache_payload());
    }

    private static function write_review_criteria_cache_file_from_payload($payload) {
        $dir = self::cache_dir_path();
        if (!file_exists($dir)) {
            wp_mkdir_p($dir);
        }
        if (!is_dir($dir) || !is_writable($dir)) {
            return false;
        }

        $json = wp_json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if (!is_string($json) || $json === '') {
            return false;
        }
        return file_put_contents(self::review_criteria_cache_file_path(), $json) !== false;
    }

    private static function review_fast_json_path() {
        $dir = self::fast_dir_path();
        if (!$dir) {
            return null;
        }
        return trailingslashit($dir) . self::REVIEW_FAST_JSON_FILE;
    }

    private static function review_fast_php_path() {
        $dir = self::fast_dir_path();
        if (!$dir) {
            return null;
        }
        return trailingslashit($dir) . self::REVIEW_FAST_PHP_FILE;
    }

    private static function write_review_criteria_fast_files() {
        $dir = self::fast_dir_path();
        $jsonPath = self::review_fast_json_path();
        $phpPath = self::review_fast_php_path();
        if (!$dir || !$jsonPath || !$phpPath) {
            return false;
        }
        if (!file_exists($dir)) {
            @wp_mkdir_p($dir);
        }
        if (!file_exists($dir) || !is_dir($dir) || !is_writable($dir)) {
            return false;
        }

        $payload = self::build_review_criteria_cache_payload();
        $json = wp_json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        if (!is_string($json) || $json === '') {
            return false;
        }

        $jsonOk = @file_put_contents($jsonPath, $json) !== false;
        if (!$jsonOk) {
            return false;
        }

        $phpStub = "<?php\n";
        $phpStub .= "header('Content-Type: application/json; charset=utf-8');\n";
        $phpStub .= "header('Cache-Control: public, max-age=300, s-maxage=300');\n";
        $phpStub .= "\$f = __DIR__ . '/" . self::REVIEW_FAST_JSON_FILE . "';\n";
        $phpStub .= "if (!is_file(\$f)) { http_response_code(404); echo '{\"status\":\"error\",\"message\":\"review criteria file not found\"}'; exit; }\n";
        $phpStub .= "readfile(\$f);\n";
        $phpStub .= "exit;\n";

        return @file_put_contents($phpPath, $phpStub) !== false;
    }

    private static function build_review_criteria_cache_payload() {
        $payload = [
            'generated_at' => gmdate('c'),
            'yith_available' => self::is_yith_reviews_available(),
            'yith_enabled' => self::is_yith_mode_enabled(),
            'manual_review_criteria' => self::get_review_criteria_rows(),
            'default_box_id' => 0,
            'criteria_terms' => [],
            'review_boxes' => [],
        ];

        if (!$payload['yith_available']) {
            return $payload;
        }

        $criteriaTerms = [];
        $criteriaTax = defined('YITH_YWAR_Post_Types::CRITERIA_TAX') ? YITH_YWAR_Post_Types::CRITERIA_TAX : 'ywar_review_criteria';
        $criteria = get_terms([
            'taxonomy' => $criteriaTax,
            'hide_empty' => false,
        ]);
        if (is_array($criteria) && !is_wp_error($criteria)) {
            foreach ($criteria as $term) {
                if (!is_object($term) || !isset($term->term_id)) {
                    continue;
                }
                $criteriaTerms[] = [
                    'id' => (int) $term->term_id,
                    'slug' => (string) $term->slug,
                    'name' => (string) $term->name,
                ];
            }
        }
        $payload['criteria_terms'] = $criteriaTerms;
        $payload['default_box_id'] = (int) get_option('yith-ywar-default-box-id', 0);

        $boxes = yith_ywar_get_review_boxes([
            'posts_per_page' => -1,
            'order_by' => 'ID',
            'order' => 'DESC',
        ]);

        $snapshotBoxes = [];
        if (is_array($boxes)) {
            foreach ($boxes as $box) {
                if (!is_object($box) || !method_exists($box, 'get_id')) {
                    continue;
                }
                $snapshotBoxes[] = [
                    'id' => (int) $box->get_id(),
                    'show_on' => method_exists($box, 'get_show_on') ? (string) $box->get_show_on() : '',
                    'active' => method_exists($box, 'get_active') ? strtoupper((string) $box->get_active()) === 'YES' : false,
                    'enable_multi_criteria' => method_exists($box, 'get_enable_multi_criteria') ? strtolower((string) $box->get_enable_multi_criteria()) === 'yes' : false,
                    'multi_criteria' => array_map('intval', method_exists($box, 'get_multi_criteria') ? (array) $box->get_multi_criteria() : []),
                    'product_ids' => array_map('intval', method_exists($box, 'get_product_ids') ? (array) $box->get_product_ids() : []),
                    'category_ids' => array_map('intval', method_exists($box, 'get_category_ids') ? (array) $box->get_category_ids() : []),
                    'tag_ids' => array_map('intval', method_exists($box, 'get_tag_ids') ? (array) $box->get_tag_ids() : []),
                ];
            }
        }
        $payload['review_boxes'] = $snapshotBoxes;

        return $payload;
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
