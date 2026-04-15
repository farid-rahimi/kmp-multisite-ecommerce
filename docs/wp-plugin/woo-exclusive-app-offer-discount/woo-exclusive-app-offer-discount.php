<?php
/**
 * Plugin Name: Woo Exclusive App Offer Discount
 * Description: Adds per-product app offer discount percent with Quick Edit and Bulk Edit support for WooCommerce products.
 * Version: 1.0.0
 * Author: Solutionium
 */

if (!defined('ABSPATH')) {
    exit;
}

final class Woo_Exclusive_App_Offer_Discount {
    private const META_KEY = '_app_offer';
    private const LEGACY_META_KEY = '_woo_app_offer_discount_percent';
    private const CLEANUP_OPTION = 'woo_app_offer_legacy_meta_cleanup_done';
    private const QUICK_NONCE_ACTION = 'woo_app_offer_quick_edit_nonce_action';
    private const QUICK_NONCE_NAME = 'woo_app_offer_quick_edit_nonce';

    public static function init(): void {
        add_action('init', [self::class, 'cleanup_legacy_meta_once']);

        add_filter('manage_edit-product_columns', [self::class, 'add_column'], 20);
        add_action('manage_product_posts_custom_column', [self::class, 'render_column'], 10, 2);

        add_action('quick_edit_custom_box', [self::class, 'render_quick_edit_field'], 10, 2);
        add_action('bulk_edit_custom_box', [self::class, 'render_bulk_edit_field'], 10, 2);
        add_action('admin_footer-edit.php', [self::class, 'print_admin_js']);

        add_action('save_post_product', [self::class, 'save_quick_edit'], 20, 2);
        add_action('woocommerce_product_bulk_edit_save', [self::class, 'save_bulk_edit']);

        // Optional full product edit field for convenience.
        add_action('woocommerce_product_options_general_product_data', [self::class, 'render_product_data_field']);
        add_action('woocommerce_process_product_meta', [self::class, 'save_product_data_field']);
    }

    public static function add_column(array $columns): array {
        $new_columns = [];
        foreach ($columns as $key => $label) {
            $new_columns[$key] = $label;
            if ($key === 'price') {
                $new_columns['woo_app_offer_discount'] = __('App Offer %', 'woo-exclusive-app-offer-discount');
            }
        }

        if (!isset($new_columns['woo_app_offer_discount'])) {
            $new_columns['woo_app_offer_discount'] = __('App Offer %', 'woo-exclusive-app-offer-discount');
        }

        return $new_columns;
    }

    public static function render_column(string $column, int $post_id): void {
        if ($column !== 'woo_app_offer_discount') {
            return;
        }

        $value = get_post_meta($post_id, self::META_KEY, true);
        $value = self::normalize_for_display($value);

        if ($value === '') {
            echo '<span class="woo-app-offer-discount" data-discount=""></span>&mdash;';
            return;
        }

        echo '<span class="woo-app-offer-discount" data-discount="' . esc_attr($value) . '">' . esc_html($value) . '%</span>';
    }

    public static function render_quick_edit_field(string $column_name, string $post_type): void {
        if ($post_type !== 'product' || $column_name !== 'woo_app_offer_discount') {
            return;
        }

        wp_nonce_field(self::QUICK_NONCE_ACTION, self::QUICK_NONCE_NAME);
        ?>
        <fieldset class="inline-edit-col-right">
            <div class="inline-edit-col">
                <label class="inline-edit-group">
                    <span class="title"><?php esc_html_e('App Offer %', 'woo-exclusive-app-offer-discount'); ?></span>
                    <span class="input-text-wrap">
                        <input type="number" min="0" max="100" step="0.01" name="woo_app_offer_discount_percent" value="" />
                    </span>
                </label>
            </div>
        </fieldset>
        <?php
    }

    public static function render_bulk_edit_field(string $column_name, string $post_type): void {
        if ($post_type !== 'product' || $column_name !== 'woo_app_offer_discount') {
            return;
        }
        ?>
        <fieldset class="inline-edit-col-right">
            <div class="inline-edit-col">
                <div class="inline-edit-group">
                    <label class="alignleft">
                        <span class="title"><?php esc_html_e('App Offer %', 'woo-exclusive-app-offer-discount'); ?></span>
                        <select name="woo_app_offer_discount_bulk_mode" class="woo-app-offer-bulk-mode">
                            <option value="keep"><?php esc_html_e('No change', 'woo-exclusive-app-offer-discount'); ?></option>
                            <option value="set"><?php esc_html_e('Set value', 'woo-exclusive-app-offer-discount'); ?></option>
                            <option value="clear"><?php esc_html_e('Clear value', 'woo-exclusive-app-offer-discount'); ?></option>
                        </select>
                    </label>
                </div>
                <div class="inline-edit-group woo-app-offer-bulk-value-wrap" style="display:none; margin-top:8px;">
                    <label class="alignleft">
                        <span class="title"><?php esc_html_e('Discount %', 'woo-exclusive-app-offer-discount'); ?></span>
                        <input type="number" min="0" max="100" step="0.01" name="woo_app_offer_discount_bulk_value" value="" />
                    </label>
                </div>
            </div>
        </fieldset>
        <?php
    }

    public static function print_admin_js(): void {
        $screen = function_exists('get_current_screen') ? get_current_screen() : null;
        if (!$screen || $screen->id !== 'edit-product') {
            return;
        }
        ?>
        <script>
        (function($){
            var $wpInlineEdit = inlineEditPost.edit;

            inlineEditPost.edit = function(id) {
                $wpInlineEdit.apply(this, arguments);

                var postId = 0;
                if (typeof id === 'object') {
                    postId = parseInt(this.getId(id), 10);
                }
                if (!postId) {
                    return;
                }

                var $row = $('#post-' + postId);
                var $editRow = $('#edit-' + postId);
                var currentDiscount = $row.find('.column-woo_app_offer_discount .woo-app-offer-discount').data('discount');

                $editRow.find('input[name="woo_app_offer_discount_percent"]').val(
                    (typeof currentDiscount !== 'undefined' && currentDiscount !== null) ? currentDiscount : ''
                );
            };

            $(document).on('change', '.woo-app-offer-bulk-mode', function() {
                var mode = $(this).val();
                var $wrap = $(this).closest('.inline-edit-col').find('.woo-app-offer-bulk-value-wrap');
                if (mode === 'set') {
                    $wrap.show();
                } else {
                    $wrap.hide();
                }
            });

            $(document).on('click', '#bulk_edit', function() {
                var $bulkRow = $('#bulk-edit');
                if ($bulkRow.length) {
                    $bulkRow.find('.woo-app-offer-bulk-mode').trigger('change');
                }
            });
        })(jQuery);
        </script>
        <?php
    }

    public static function save_quick_edit(int $post_id, WP_Post $post): void {
        if ($post->post_type !== 'product') {
            return;
        }

        if (!current_user_can('edit_post', $post_id)) {
            return;
        }

        if (!isset($_POST[self::QUICK_NONCE_NAME]) || !wp_verify_nonce(sanitize_text_field(wp_unslash($_POST[self::QUICK_NONCE_NAME])), self::QUICK_NONCE_ACTION)) {
            return;
        }

        if (!isset($_POST['woo_app_offer_discount_percent'])) {
            return;
        }

        $raw = sanitize_text_field(wp_unslash($_POST['woo_app_offer_discount_percent']));
        self::save_discount_meta($post_id, $raw);
    }

    public static function save_bulk_edit(WC_Product $product): void {
        if (!isset($_REQUEST['woo_app_offer_discount_bulk_mode'])) {
            return;
        }

        $mode = sanitize_text_field(wp_unslash($_REQUEST['woo_app_offer_discount_bulk_mode']));

        if ($mode === 'keep') {
            return;
        }

        $product_id = $product->get_id();
        if (!current_user_can('edit_post', $product_id)) {
            return;
        }

        if ($mode === 'clear') {
            delete_post_meta($product_id, self::META_KEY);
            return;
        }

        if ($mode === 'set') {
            $raw = isset($_REQUEST['woo_app_offer_discount_bulk_value'])
                ? sanitize_text_field(wp_unslash($_REQUEST['woo_app_offer_discount_bulk_value']))
                : '';
            self::save_discount_meta($product_id, $raw);
        }
    }

    public static function render_product_data_field(): void {
        woocommerce_wp_text_input([
            'id' => self::META_KEY,
            'label' => __('App Offer Discount %', 'woo-exclusive-app-offer-discount'),
            'description' => __('Exclusive in-app offer discount percent (0-100).', 'woo-exclusive-app-offer-discount'),
            'desc_tip' => true,
            'type' => 'number',
            'custom_attributes' => [
                'step' => '0.01',
                'min' => '0',
                'max' => '100',
            ],
        ]);
    }

    public static function save_product_data_field(int $post_id): void {
        if (!isset($_POST[self::META_KEY])) {
            return;
        }
        $raw = sanitize_text_field(wp_unslash($_POST[self::META_KEY]));
        self::save_discount_meta($post_id, $raw);
    }

    private static function save_discount_meta(int $product_id, string $raw): void {
        $raw = trim($raw);
        if ($raw === '') {
            delete_post_meta($product_id, self::META_KEY);
            return;
        }

        $value = (float) $raw;
        if ($value < 0) {
            $value = 0;
        }
        if ($value > 100) {
            $value = 100;
        }

        update_post_meta($product_id, self::META_KEY, self::normalize_for_storage($value));
    }

    public static function cleanup_legacy_meta_once(): void {
        if (get_option(self::CLEANUP_OPTION) === 'yes') {
            return;
        }

        global $wpdb;
        if (!($wpdb instanceof wpdb)) {
            return;
        }

        // Hard-delete legacy app-offer key so Woo REST meta_data no longer includes it.
        $wpdb->delete($wpdb->postmeta, ['meta_key' => self::LEGACY_META_KEY], ['%s']);
        update_option(self::CLEANUP_OPTION, 'yes', false);
    }

    private static function normalize_for_storage(float $value): string {
        $normalized = rtrim(rtrim(number_format($value, 2, '.', ''), '0'), '.');
        return $normalized === '' ? '0' : $normalized;
    }

    private static function normalize_for_display($value): string {
        if ($value === '' || $value === null) {
            return '';
        }

        $number = (float) $value;
        return self::normalize_for_storage($number);
    }
}

Woo_Exclusive_App_Offer_Discount::init();
