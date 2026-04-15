<?php
/**
 * Plugin Name: Solu Mobile Auth Login Endpoint
 * Description: Adds /wp-json/woo-mobile-auth/v1/login_user endpoint and Bearer-token auth compatibility for mobile apps.
 * Version: 1.2.0
 * Author: Solutionium
 */

if (!defined('ABSPATH')) {
    exit;
}

final class Woo_Mobile_Auth_Login_Endpoint {
    private const META_TOKEN_HASH = '_woo_mobile_auth_token_hash';
    private const META_TOKEN_EXP = '_woo_mobile_auth_token_exp';
    private const TOKEN_META_PREFIX = '_woo_mobile_auth_token_';
    private const MAX_ACTIVE_TOKENS_PER_USER = 20;
    private const META_RESET_OTP_HASH = '_woo_mobile_auth_reset_otp_hash';
    private const META_RESET_OTP_EXP = '_woo_mobile_auth_reset_otp_exp';
    private const META_RESET_OTP_VERIFIED = '_woo_mobile_auth_reset_otp_verified';
    private const META_DELETE_OTP_HASH = '_woo_mobile_auth_delete_otp_hash';
    private const META_DELETE_OTP_EXP = '_woo_mobile_auth_delete_otp_exp';
    private const SIGNUP_OTP_TRANSIENT_PREFIX = 'woo_mobile_signup_otp_';
    private const SIGNUP_OTP_VERIFIED_PREFIX = 'woo_mobile_signup_verified_';
    private const TOKEN_TTL_SECONDS = 30 * DAY_IN_SECONDS;
    private const RESET_OTP_TTL_SECONDS = 10 * MINUTE_IN_SECONDS;
    private const MOBILE_PAY_TOKEN_TTL_SECONDS = 5 * MINUTE_IN_SECONDS;
    private const MOBILE_PAY_TRANSIENT_PREFIX = 'woo_mobile_pay_';
    private const MOBILE_RETURN_META = '_woo_mobile_return_to_app';
    private const MOBILE_RETURN_EXPIRES_META = '_woo_mobile_return_expires';
    private const MOBILE_RETURN_SCHEME_META = '_woo_mobile_return_scheme';
    private const META_FAVORITES_IDS = '_woo_mobile_favorite_ids';
    private const META_FAVORITES_UPDATED_AT = '_woo_mobile_favorite_updated_at';
    private const MOBILE_RETURN_ORDER_COOKIE = 'woo_mobile_return_order';
    private const MOBILE_RETURN_TTL_SECONDS = 2 * HOUR_IN_SECONDS;
    private const DEFAULT_MOBILE_APP_SCHEME = 'solutioniuma';

    public static function init(): void {
        add_action('rest_api_init', [self::class, 'register_routes']);
        add_action('rest_api_init', [self::class, 'register_user_rest_fields']);
        add_filter('determine_current_user', [self::class, 'authenticate_bearer_token'], 20);
        // Run late to ensure third-party plugins cannot re-add unrelated gateways on order-pay.
        add_filter('woocommerce_available_payment_gateways', [self::class, 'filter_order_pay_gateways'], 9999);
        add_filter('woocommerce_order_pay_requires_login', [self::class, 'allow_order_pay_for_mobile_token'], 10, 2);
        add_filter('woocommerce_get_return_url', [self::class, 'filter_mobile_gateway_return_url'], 10, 2);
        add_action('wp', [self::class, 'handle_mobile_pay_autopay_entry'], 0);
        add_action('template_redirect', [self::class, 'handle_mobile_order_pay_auto_redirect'], 1);
        add_action('template_redirect', [self::class, 'handle_mobile_payment_return_redirect'], 2);
    }

    public static function register_user_rest_fields(): void {
        register_rest_field('user', 'first_name', [
            'get_callback' => [self::class, 'get_user_first_name'],
            'schema' => [
                'description' => 'User first name',
                'type' => 'string',
                'context' => ['view', 'edit'],
            ],
        ]);

        register_rest_field('user', 'last_name', [
            'get_callback' => [self::class, 'get_user_last_name'],
            'schema' => [
                'description' => 'User last name',
                'type' => 'string',
                'context' => ['view', 'edit'],
            ],
        ]);

        register_rest_field('user', 'email', [
            'get_callback' => [self::class, 'get_user_email'],
            'schema' => [
                'description' => 'User email',
                'type' => 'string',
                'context' => ['view', 'edit'],
            ],
        ]);

        register_rest_field('user', 'phone_number', [
            'get_callback' => [self::class, 'get_user_phone_number'],
            'schema' => [
                'description' => 'User phone number',
                'type' => 'string',
                'context' => ['view', 'edit'],
            ],
        ]);
    }

    public static function register_routes(): void {
        register_rest_route('woo-mobile-auth/v1', '/login_user', [
            'methods' => 'POST',
            'callback' => [self::class, 'login_user'],
            'permission_callback' => '__return_true',
            'args' => [
                'user' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'password' => [
                    'required' => true,
                    'type' => 'string',
                ],
            ],
        ]);

        register_rest_route('woo-mobile-auth/v1', '/register_user', [
            'methods' => 'POST',
            'callback' => [self::class, 'register_user'],
            'permission_callback' => '__return_true',
            'args' => [
                'name' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'email' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'phone' => [
                    'required' => false,
                    'type' => 'string',
                ],
                'password' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'require_email_otp' => [
                    'required' => false,
                    'type' => 'string',
                ],
            ],
        ]);

        register_rest_route('woo-mobile-auth/v1', '/request_password_otp', [
            'methods' => 'POST',
            'callback' => [self::class, 'request_password_otp'],
            'permission_callback' => '__return_true',
            'args' => [
                'email' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'mode' => [
                    'required' => false,
                    'type' => 'string',
                ],
            ],
        ]);

        register_rest_route('woo-mobile-auth/v1', '/verify_password_otp', [
            'methods' => 'POST',
            'callback' => [self::class, 'verify_password_otp'],
            'permission_callback' => '__return_true',
            'args' => [
                'email' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'otp' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'mode' => [
                    'required' => false,
                    'type' => 'string',
                ],
            ],
        ]);

        register_rest_route('woo-mobile-auth/v1', '/reset_password_with_otp', [
            'methods' => 'POST',
            'callback' => [self::class, 'reset_password_with_otp'],
            'permission_callback' => '__return_true',
            'args' => [
                'email' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'otp' => [
                    'required' => true,
                    'type' => 'string',
                ],
                'new_password' => [
                    'required' => true,
                    'type' => 'string',
                ],
            ],
        ]);

        register_rest_route('woo-mobile-auth/v1', '/update_profile', [
            'methods' => 'POST',
            'callback' => [self::class, 'update_profile'],
            'permission_callback' => '__return_true',
        ]);

        register_rest_route('woo-mobile-auth/v1', '/logout_user', [
            'methods' => 'POST',
            'callback' => [self::class, 'logout_user'],
            'permission_callback' => '__return_true',
        ]);

        register_rest_route('woo-mobile-auth/v1', '/request_delete_account_otp', [
            'methods' => 'POST',
            'callback' => [self::class, 'request_delete_account_otp'],
            'permission_callback' => '__return_true',
        ]);

        register_rest_route('woo-mobile-auth/v1', '/delete_account', [
            'methods' => 'POST',
            'callback' => [self::class, 'delete_account'],
            'permission_callback' => '__return_true',
            'args' => [
                'method' => [
                    'required' => false,
                    'type' => 'string',
                ],
                'password' => [
                    'required' => false,
                    'type' => 'string',
                ],
                'otp' => [
                    'required' => false,
                    'type' => 'string',
                ],
            ],
        ]);

        register_rest_route('woo-mobile-auth/v1', '/favorites', [
            'methods' => 'GET',
            'callback' => [self::class, 'favorites_get'],
            'permission_callback' => '__return_true',
        ]);

        register_rest_route('woo-mobile-auth/v1', '/favorites/toggle', [
            'methods' => 'POST',
            'callback' => [self::class, 'favorites_toggle'],
            'permission_callback' => '__return_true',
            'args' => [
                'product_id' => [
                    'required' => true,
                    'type' => 'integer',
                ],
                'is_favorite' => [
                    'required' => true,
                    'type' => 'string',
                ],
            ],
        ]);

        register_rest_route('woo-mobile-auth/v1', '/favorites/set', [
            'methods' => 'POST',
            'callback' => [self::class, 'favorites_set'],
            'permission_callback' => '__return_true',
        ]);

        register_rest_route('woo-mobile-auth/v1', '/payment_session_url', [
            'methods' => 'POST',
            'callback' => [self::class, 'create_payment_session_url'],
            'permission_callback' => '__return_true',
            'args' => [
                'order_id' => [
                    'required' => true,
                    'type' => 'integer',
                ],
                'order_key' => [
                    'required' => false,
                    'type' => 'string',
                ],
                'app_scheme' => [
                    'required' => false,
                    'type' => 'string',
                ],
            ],
        ]);

        register_rest_route('woo-mobile-auth/v1', '/payment_return', [
            'methods' => 'GET',
            'callback' => [self::class, 'handle_payment_return'],
            'permission_callback' => '__return_true',
        ]);
    }

    public static function login_user(WP_REST_Request $request): WP_REST_Response {
        $identifier = trim((string) $request->get_param('user'));
        $password = (string) $request->get_param('password');

        if ($identifier === '' || $password === '') {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'empty_credentials',
                    'msg' => 'Username/email and password are required.',
                ],
            ], 400);
        }

        $username = $identifier;
        if (is_email($identifier)) {
            $email_user = get_user_by('email', $identifier);
            if (!($email_user instanceof WP_User)) {
                return new WP_REST_Response([
                    'success' => false,
                    'data' => [
                        'code' => 'invalid_credentials',
                        'msg' => 'Invalid username/email or password.',
                    ],
                ], 401);
            }
            $username = (string) $email_user->user_login;
        }

        $user = wp_authenticate($username, $password);

        if (is_wp_error($user)) {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'invalid_credentials',
                    'msg' => $user->get_error_message() ?: 'Invalid username/email or password.',
                ],
            ], 401);
        }

        if (!($user instanceof WP_User)) {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'unknown_error',
                    'msg' => 'Login failed.',
                ],
            ], 500);
        }

        $token = self::generate_token();
        $token_hash = hash('sha256', $token);
        $expires_at = time() + self::TOKEN_TTL_SECONDS;

        self::store_user_token($user->ID, $token_hash, $expires_at);

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'message' => 'Login successful.',
                'user_id' => (string) $user->ID,
                'access_token' => $token,
                'token_type' => 'Bearer',
                'action' => 'login',
            ],
        ], 200);
    }

    public static function register_user(WP_REST_Request $request): WP_REST_Response {
        $name = trim((string) $request->get_param('name'));
        $email = trim((string) $request->get_param('email'));
        $phone = trim((string) $request->get_param('phone'));
        $password = (string) $request->get_param('password');
        $require_email_otp = self::to_bool($request->get_param('require_email_otp'));

        if ($name === '' || $email === '' || $password === '' || (!$require_email_otp && $phone === '')) {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'missing_fields',
                    'msg' => $require_email_otp
                        ? 'Name, email and password are required.'
                        : 'Name, email, phone and password are required.',
                ],
            ], 400);
        }

        if (!is_email($email)) {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'invalid_email',
                    'msg' => 'Please provide a valid email address.',
                ],
            ], 400);
        }

        if (email_exists($email)) {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'email_exists',
                    'msg' => 'An account already exists with this email.',
                ],
            ], 409);
        }

        if ($require_email_otp && !self::is_signup_email_verified($email)) {
            return self::error_response('email_not_verified', 'Please verify your email with OTP before signing up.', 400);
        }

        if (strlen($password) < 6) {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'weak_password',
                    'msg' => 'Password must be at least 6 characters.',
                ],
            ], 400);
        }

        $username = self::generate_username_from_email($email);
        if (username_exists($username)) {
            $username = self::generate_unique_username($username);
        }

        $user_id = wp_create_user($username, $password, $email);
        if (is_wp_error($user_id)) {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'user_creation_failed',
                    'msg' => $user_id->get_error_message() ?: 'Could not create account.',
                ],
            ], 500);
        }

        wp_update_user([
            'ID' => $user_id,
            'display_name' => $name,
            'first_name' => $name,
        ]);
        if ($phone !== '') {
            update_user_meta($user_id, 'billing_phone', $phone);
            update_user_meta($user_id, 'phone_number', $phone);
        }

        $token = self::generate_token();
        $token_hash = hash('sha256', $token);
        $expires_at = time() + self::TOKEN_TTL_SECONDS;

        self::store_user_token($user_id, $token_hash, $expires_at);
        if ($require_email_otp) {
            self::clear_signup_otp($email);
            self::clear_signup_verified($email);
        }

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'message' => 'Registration successful.',
                'user_id' => (string) $user_id,
                'access_token' => $token,
                'token_type' => 'Bearer',
                'action' => 'register',
            ],
        ], 200);
    }

    public static function request_password_otp(WP_REST_Request $request): WP_REST_Response {
        $email = trim((string) $request->get_param('email'));
        $mode = self::normalize_otp_mode((string) $request->get_param('mode'));
        if (!is_email($email)) {
            return self::error_response('invalid_email', 'Please provide a valid email address.', 400);
        }

        $otp = $mode === 'signup'
            ? (string) random_int(1000, 9999)
            : (string) random_int(100000, 999999);
        if ($mode === 'signup') {
            if (email_exists($email)) {
                return self::error_response('email_exists', 'An account already exists with this email.', 409);
            }

            self::store_signup_otp($email, $otp);

            $subject = 'Your Sign Up Verification Code';
            $message = "Your sign up verification code is: {$otp}\n\nThis code expires in 10 minutes.";
            $headers = ['Content-Type: text/plain; charset=UTF-8'];
            $sent = wp_mail($email, $subject, $message, $headers);
            if (!$sent) {
                return self::error_response('email_send_failed', 'Could not send verification code email.', 500);
            }

            return new WP_REST_Response([
                'success' => true,
                'data' => [
                    'message' => 'Verification code sent successfully.',
                ],
            ], 200);
        }

        $user = get_user_by('email', $email);
        if (!($user instanceof WP_User)) {
            return self::error_response('user_not_found', 'No account found with this email.', 404);
        }

        $otp_hash = wp_hash_password($otp);
        $expires_at = time() + self::RESET_OTP_TTL_SECONDS;

        update_user_meta($user->ID, self::META_RESET_OTP_HASH, $otp_hash);
        update_user_meta($user->ID, self::META_RESET_OTP_EXP, $expires_at);
        update_user_meta($user->ID, self::META_RESET_OTP_VERIFIED, 0);

        $subject = 'Your Password Reset Code';
        $message = "Your verification code is: {$otp}\n\nThis code expires in 10 minutes.";
        $headers = ['Content-Type: text/plain; charset=UTF-8'];
        $sent = wp_mail($email, $subject, $message, $headers);
        if (!$sent) {
            return self::error_response('email_send_failed', 'Could not send verification code email.', 500);
        }

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'message' => 'Verification code sent successfully.',
            ],
        ], 200);
    }

    public static function verify_password_otp(WP_REST_Request $request): WP_REST_Response {
        $email = trim((string) $request->get_param('email'));
        $otp = trim((string) $request->get_param('otp'));
        $mode = self::normalize_otp_mode((string) $request->get_param('mode'));

        if (!is_email($email) || $otp === '') {
            return self::error_response('invalid_request', 'Email and OTP are required.', 400);
        }

        if ($mode === 'signup') {
            $validation = self::validate_signup_otp($email, $otp);
            if ($validation !== true) {
                return $validation;
            }
            self::set_signup_verified($email);

            return new WP_REST_Response([
                'success' => true,
                'data' => [
                    'message' => 'Verification code confirmed.',
                ],
            ], 200);
        }

        $user = get_user_by('email', $email);
        if (!($user instanceof WP_User)) {
            return self::error_response('user_not_found', 'No account found with this email.', 404);
        }

        $validation = self::validate_user_otp($user->ID, $otp);
        if ($validation !== true) {
            return $validation;
        }

        update_user_meta($user->ID, self::META_RESET_OTP_VERIFIED, 1);

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'message' => 'Verification code confirmed.',
            ],
        ], 200);
    }

    public static function reset_password_with_otp(WP_REST_Request $request): WP_REST_Response {
        $email = trim((string) $request->get_param('email'));
        $otp = trim((string) $request->get_param('otp'));
        $new_password = (string) $request->get_param('new_password');

        if (!is_email($email) || $otp === '' || $new_password === '') {
            return self::error_response('invalid_request', 'Email, OTP and new password are required.', 400);
        }
        if (strlen($new_password) < 6) {
            return self::error_response('weak_password', 'Password must be at least 6 characters.', 400);
        }

        $user = get_user_by('email', $email);
        if (!($user instanceof WP_User)) {
            return self::error_response('user_not_found', 'No account found with this email.', 404);
        }

        $validation = self::validate_user_otp($user->ID, $otp);
        if ($validation !== true) {
            return $validation;
        }

        $verified = (int) get_user_meta($user->ID, self::META_RESET_OTP_VERIFIED, true);
        if ($verified !== 1) {
            return self::error_response('otp_not_verified', 'Please verify the OTP code first.', 400);
        }

        wp_set_password($new_password, $user->ID);
        delete_user_meta($user->ID, self::META_RESET_OTP_HASH);
        delete_user_meta($user->ID, self::META_RESET_OTP_EXP);
        delete_user_meta($user->ID, self::META_RESET_OTP_VERIFIED);

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'message' => 'Password updated successfully.',
            ],
        ], 200);
    }

    public static function update_profile(WP_REST_Request $request): WP_REST_Response {
        $user_id = get_current_user_id();
        if ($user_id <= 0) {
            return self::error_response('rest_not_logged_in', 'Authentication required.', 401);
        }

        $user = get_user_by('id', $user_id);
        if (!($user instanceof WP_User)) {
            return self::error_response('user_not_found', 'User not found.', 404);
        }

        $first_name = trim((string) $request->get_param('first_name'));
        $last_name = trim((string) $request->get_param('last_name'));
        $display_name = trim((string) $request->get_param('name'));
        $nickname = trim((string) $request->get_param('nickname'));
        $email = trim((string) $request->get_param('email'));
        $phone_number = trim((string) $request->get_param('phone_number'));
        if ($phone_number === '') {
            $phone_number = trim((string) $request->get_param('phone'));
        }

        if ($email !== '' && !is_email($email)) {
            return self::error_response('invalid_email', 'Please provide a valid email address.', 400);
        }

        if ($email !== '' && strcasecmp($email, (string) $user->user_email) !== 0 && email_exists($email)) {
            return self::error_response('email_exists', 'This email is already used by another account.', 409);
        }

        $update_args = ['ID' => $user_id];
        if ($first_name !== '') {
            $update_args['first_name'] = $first_name;
        }
        if ($last_name !== '') {
            $update_args['last_name'] = $last_name;
        }
        if ($display_name !== '') {
            $update_args['display_name'] = $display_name;
        }
        if ($nickname !== '') {
            $update_args['nickname'] = $nickname;
        }
        if ($email !== '') {
            $update_args['user_email'] = $email;
        }

        if (count($update_args) > 1) {
            $result = wp_update_user($update_args);
            if (is_wp_error($result)) {
                return self::error_response(
                    'profile_update_failed',
                    $result->get_error_message() ?: 'Could not update profile.',
                    500
                );
            }
        }

        if ($phone_number !== '') {
            update_user_meta($user_id, 'phone_number', $phone_number);
            update_user_meta($user_id, 'billing_phone', $phone_number);
        }

        $meta = $request->get_param('meta');
        if (is_array($meta) && isset($meta['_fcm_token'])) {
            $fcm = trim((string) $meta['_fcm_token']);
            if ($fcm !== '') {
                update_user_meta($user_id, '_fcm_token', $fcm);
            }
        }

        $updated = get_userdata($user_id);
        if (!($updated instanceof WP_User)) {
            return self::error_response('user_not_found', 'User not found after update.', 404);
        }

        $updated_first_name = (string) get_user_meta($user_id, 'first_name', true);
        $updated_last_name = (string) get_user_meta($user_id, 'last_name', true);
        $updated_phone = (string) get_user_meta($user_id, 'phone_number', true);
        if ($updated_phone === '') {
            $updated_phone = (string) get_user_meta($user_id, 'billing_phone', true);
        }

        return new WP_REST_Response([
            'id' => (int) $updated->ID,
            'name' => (string) $updated->display_name,
            'first_name' => $updated_first_name,
            'last_name' => $updated_last_name,
            'nickname' => (string) $updated->nickname,
            'email' => (string) $updated->user_email,
            'phone_number' => $updated_phone,
            'is_super_admin' => user_can($updated->ID, 'manage_options'),
        ], 200);
    }

    public static function logout_user(WP_REST_Request $request): WP_REST_Response {
        $user_id = get_current_user_id();
        if ($user_id <= 0) {
            return self::error_response('rest_not_logged_in', 'Authentication required.', 401);
        }

        $token = self::extract_bearer_token();
        if ($token === '') {
            return self::error_response('missing_token', 'Bearer token is required.', 401);
        }

        $token_hash = hash('sha256', $token);
        delete_user_meta($user_id, self::TOKEN_META_PREFIX . $token_hash);

        $latest_hash = (string) get_user_meta($user_id, self::META_TOKEN_HASH, true);
        if ($latest_hash !== '' && hash_equals($latest_hash, $token_hash)) {
            delete_user_meta($user_id, self::META_TOKEN_HASH);
            delete_user_meta($user_id, self::META_TOKEN_EXP);
        }

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'message' => 'Logged out successfully.',
            ],
        ], 200);
    }

    public static function request_delete_account_otp(WP_REST_Request $request): WP_REST_Response {
        $user_id = get_current_user_id();
        if ($user_id <= 0) {
            return self::error_response('rest_not_logged_in', 'Authentication required.', 401);
        }

        $user = get_user_by('id', $user_id);
        if (!($user instanceof WP_User)) {
            return self::error_response('user_not_found', 'User not found.', 404);
        }

        $email = trim((string) $user->user_email);
        if (!is_email($email)) {
            return self::error_response('invalid_email', 'No valid email found for this account.', 400);
        }

        $otp = (string) random_int(1000, 9999);
        update_user_meta($user_id, self::META_DELETE_OTP_HASH, wp_hash_password($otp));
        update_user_meta($user_id, self::META_DELETE_OTP_EXP, time() + self::RESET_OTP_TTL_SECONDS);

        $subject = 'Your Account Deletion Verification Code';
        $message = "Your verification code is: {$otp}\n\nThis code expires in 10 minutes.";
        $headers = ['Content-Type: text/plain; charset=UTF-8'];
        $sent = wp_mail($email, $subject, $message, $headers);

        if (!$sent) {
            return self::error_response('email_send_failed', 'Could not send verification code email.', 500);
        }

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'message' => 'Verification code sent successfully.',
            ],
        ], 200);
    }

    public static function delete_account(WP_REST_Request $request): WP_REST_Response {
        $user_id = get_current_user_id();
        if ($user_id <= 0) {
            return self::error_response('rest_not_logged_in', 'Authentication required.', 401);
        }

        $user = get_user_by('id', $user_id);
        if (!($user instanceof WP_User)) {
            return self::error_response('user_not_found', 'User not found.', 404);
        }

        $method = strtolower(trim((string) $request->get_param('method')));
        if ($method === '') {
            $method = 'password';
        }

        if ($method === 'otp') {
            $otp = trim((string) $request->get_param('otp'));
            if ($otp === '') {
                return self::error_response('invalid_request', 'Verification code is required.', 400);
            }

            $validation = self::validate_delete_otp($user_id, $otp);
            if ($validation !== true) {
                return $validation;
            }
        } else {
            $password = (string) $request->get_param('password');
            if ($password === '') {
                return self::error_response('invalid_request', 'Password is required.', 400);
            }
            if (!wp_check_password($password, (string) $user->user_pass, $user_id)) {
                return self::error_response('invalid_password', 'Incorrect password.', 401);
            }
        }

        self::anonymize_user_orders($user_id);
        self::revoke_all_user_tokens($user_id);
        self::clear_user_sensitive_meta($user_id);
        wp_logout();

        if (!function_exists('wp_delete_user')) {
            require_once ABSPATH . 'wp-admin/includes/user.php';
        }

        $reassign_user_id = self::resolve_reassign_user_id($user_id);
        $deleted = wp_delete_user($user_id, $reassign_user_id > 0 ? $reassign_user_id : null);

        if (!$deleted) {
            return self::error_response('delete_failed', 'Could not delete account at this time.', 500);
        }

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'message' => 'Account deleted successfully.',
            ],
        ], 200);
    }

    public static function favorites_get(WP_REST_Request $request): WP_REST_Response {
        $user_id = get_current_user_id();
        if ($user_id <= 0) {
            return self::error_response('rest_not_logged_in', 'Authentication required.', 401);
        }

        $payload = self::get_user_favorites_payload($user_id);
        return new WP_REST_Response([
            'success' => true,
            'data' => $payload,
        ], 200);
    }

    public static function favorites_toggle(WP_REST_Request $request): WP_REST_Response {
        $user_id = get_current_user_id();
        if ($user_id <= 0) {
            return self::error_response('rest_not_logged_in', 'Authentication required.', 401);
        }

        $product_id = absint($request->get_param('product_id'));
        if ($product_id <= 0) {
            return self::error_response('invalid_product_id', 'Valid product_id is required.', 400);
        }

        $raw_is_favorite = $request->get_param('is_favorite');
        $is_favorite = false;
        if (is_bool($raw_is_favorite)) {
            $is_favorite = $raw_is_favorite;
        } else {
            $is_favorite = in_array(strtolower(trim((string) $raw_is_favorite)), ['1', 'true', 'yes', 'on'], true);
        }

        $ids = self::get_user_favorite_ids($user_id);
        $ids_assoc = array_fill_keys($ids, true);
        if ($is_favorite) {
            $ids_assoc[$product_id] = true;
        } else {
            unset($ids_assoc[$product_id]);
        }
        $normalized = array_keys($ids_assoc);
        sort($normalized, SORT_NUMERIC);

        self::save_user_favorite_ids($user_id, $normalized);

        return new WP_REST_Response([
            'success' => true,
            'data' => self::get_user_favorites_payload($user_id),
        ], 200);
    }

    public static function favorites_set(WP_REST_Request $request): WP_REST_Response {
        $user_id = get_current_user_id();
        if ($user_id <= 0) {
            return self::error_response('rest_not_logged_in', 'Authentication required.', 401);
        }

        $raw_ids = $request->get_param('ids');
        $ids = [];
        if (is_array($raw_ids)) {
            $ids = $raw_ids;
        } else {
            $csv = trim((string) $raw_ids);
            if ($csv !== '') {
                $ids = explode(',', $csv);
            }
        }

        $normalized = [];
        foreach ($ids as $id) {
            $value = absint($id);
            if ($value > 0) {
                $normalized[$value] = true;
            }
        }
        $normalized_ids = array_keys($normalized);
        sort($normalized_ids, SORT_NUMERIC);

        self::save_user_favorite_ids($user_id, $normalized_ids);

        return new WP_REST_Response([
            'success' => true,
            'data' => self::get_user_favorites_payload($user_id),
        ], 200);
    }

    public static function create_payment_session_url(WP_REST_Request $request): WP_REST_Response {
        if (!function_exists('wc_get_order')) {
            return self::error_response('woocommerce_missing', 'WooCommerce is not available.', 500);
        }

        $user_id = get_current_user_id();

        $order_id = absint($request->get_param('order_id'));
        if ($order_id <= 0) {
            return self::error_response('invalid_order_id', 'Valid order_id is required.', 400);
        }

        $order = wc_get_order($order_id);
        if (!($order instanceof WC_Order)) {
            return self::error_response('order_not_found', 'Order not found.', 404);
        }

        $provided_key = trim((string) $request->get_param('order_key'));
        $order_key = $provided_key !== '' ? wc_clean($provided_key) : '';
        if ($order_key === '' || $order_key !== (string) $order->get_order_key()) {
            return self::error_response('invalid_order_key', 'Invalid order key.', 403);
        }
        $app_scheme = self::sanitize_app_scheme((string) $request->get_param('app_scheme'));
        if ($app_scheme === '') {
            $app_scheme = self::DEFAULT_MOBILE_APP_SCHEME;
        }

        $customer_id = (int) $order->get_customer_id();
        if ($user_id > 0 && $customer_id > 0 && $customer_id !== $user_id) {
            return self::error_response('forbidden_order', 'You do not have access to this order.', 403);
        }

        self::mark_order_for_mobile_return($order, $app_scheme);

        $gateway_id = (string) $order->get_payment_method();
        if (self::is_tabby_gateway_id($gateway_id)) {
            $direct_gateway_url = self::resolve_gateway_redirect_url($order);
            if ($direct_gateway_url !== '') {
                self::mobile_pay_log('Payment session returning direct Tabby URL.', [
                    'order_id' => $order_id,
                    'gateway_id' => $gateway_id,
                ]);
                return new WP_REST_Response([
                    'success' => true,
                    'data' => [
                        'payment_url' => $direct_gateway_url,
                        'order_id' => $order_id,
                        'expires_in' => self::MOBILE_PAY_TOKEN_TTL_SECONDS,
                    ],
                ], 200);
            }

            self::mobile_pay_log('Payment session Tabby direct URL failed; falling back to tokenized order-pay URL.', [
                'order_id' => $order_id,
                'gateway_id' => $gateway_id,
            ]);
        }

        // Default path (non-Tabby): tokenized /order-pay autopay flow so gateway redirect
        // is resolved in checkout page context and return-to-app logic is handled by hooks.
        self::mobile_pay_log('Payment session using tokenized order-pay autopay URL.', [
            'order_id' => $order_id,
            'gateway_id' => (string) $order->get_payment_method(),
        ]);

        $session_token = self::generate_token();
        $payload = [
            'user_id' => $user_id > 0 ? $user_id : $customer_id,
            'order_id' => $order_id,
            'order_key' => $order_key,
            'expires_at' => time() + self::MOBILE_PAY_TOKEN_TTL_SECONDS,
        ];
        set_transient(
            self::MOBILE_PAY_TRANSIENT_PREFIX . $session_token,
            $payload,
            self::MOBILE_PAY_TOKEN_TTL_SECONDS
        );

        $payment_url = add_query_arg(
            [
                'pay_for_order' => 'true',
                'key' => $order_key,
                'mobile_pay_token' => $session_token,
                'autopay' => '1',
            ],
            wc_get_checkout_url() . 'order-pay/' . $order_id . '/'
        );

        return new WP_REST_Response([
            'success' => true,
            'data' => [
                'payment_url' => $payment_url,
                'order_id' => $order_id,
                'expires_in' => self::MOBILE_PAY_TOKEN_TTL_SECONDS,
            ],
        ], 200);
    }

    private static function is_tabby_gateway_id(string $gateway_id): bool {
        $normalized = strtolower(trim($gateway_id));
        return $normalized !== '' && strpos($normalized, 'tabby') !== false;
    }

    public static function handle_payment_return(WP_REST_Request $request): WP_REST_Response {
        if (!function_exists('wc_get_order')) {
            return self::error_response('woocommerce_missing', 'WooCommerce is not available.', 500);
        }

        $requested_order_key = wc_clean((string) $request->get_param('key'));
        $order = self::get_order_from_request(
            absint($request->get_param('order_id')),
            $requested_order_key
        );

        if (!($order instanceof WC_Order)) {
            return self::error_response('order_not_found', 'Order not found.', 404);
        }
        if ($requested_order_key !== '' && $requested_order_key !== (string) $order->get_order_key()) {
            return self::error_response('invalid_order_key', 'Invalid order key.', 403);
        }

        $status = self::normalize_mobile_payment_status(
            (string) $request->get_param('status'),
            $order
        );

        $deep_link = self::build_mobile_return_deeplink(
            $order->get_id(),
            $status,
            self::get_mobile_return_scheme_for_order($order)
        );
        self::mobile_pay_log('Payment return endpoint redirecting to app.', [
            'order_id' => $order->get_id(),
            'status' => $status,
        ]);

        wp_redirect($deep_link);
        exit;
    }

    public static function filter_mobile_gateway_return_url($return_url, $order) {
        if (!($order instanceof WC_Order)) {
            return $return_url;
        }
        if (!self::is_mobile_return_enabled_for_order($order)) {
            return $return_url;
        }

        return self::build_mobile_return_proxy_url(
            $order->get_id(),
            (string) $order->get_order_key(),
            'success'
        );
    }

    public static function handle_mobile_payment_return_redirect(): void {
        if (
            !function_exists('is_checkout') ||
            !function_exists('is_checkout_pay_page') ||
            !function_exists('is_order_received_page')
        ) {
            return;
        }

        if (
            isset($_GET['autopay']) && (string) $_GET['autopay'] === '1' &&
            isset($_GET['mobile_pay_token'])
        ) {
            return;
        }

        if (
            !is_order_received_page() &&
            !is_checkout_pay_page() &&
            !self::is_checkout_like_return_request() &&
            !self::has_mobile_return_order_hint()
        ) {
            return;
        }

        $order = self::resolve_mobile_return_order();
        if (!($order instanceof WC_Order)) {
            return;
        }
        if (!self::is_mobile_return_enabled_for_order($order)) {
            return;
        }

        if (!self::should_redirect_mobile_return_now($order)) {
            self::mobile_pay_log('Template redirect skipped: waiting for gateway callback signal.', [
                'order_id' => $order->get_id(),
            ]);
            return;
        }

        $status = self::resolve_mobile_return_status_from_request($order);
        $deep_link = self::build_mobile_return_deeplink(
            $order->get_id(),
            $status,
            self::get_mobile_return_scheme_for_order($order)
        );
        self::mobile_pay_log('Template redirect returning payment result to app.', [
            'order_id' => $order->get_id(),
            'status' => $status,
        ]);

        self::clear_mobile_return_order_cookie();
        wp_redirect($deep_link);
        exit;
    }

    public static function authenticate_bearer_token($user_id) {
        if (!empty($user_id)) {
            return $user_id;
        }

        $mobile_user_id = self::resolve_mobile_pay_user_from_request();
        if ($mobile_user_id > 0) {
            return $mobile_user_id;
        }

        $token = self::extract_bearer_token();
        if ($token === '') {
            return $user_id;
        }

        $token_hash = hash('sha256', $token);
        $now = time();

        $token_user_id = self::find_user_id_by_token_hash($token_hash, $now);
        if ($token_user_id > 0) {
            return $token_user_id;
        }

        // Backward compatibility with old single-token storage.
        $users = get_users([
            'number' => 1,
            'fields' => 'ID',
            'meta_query' => [
                'relation' => 'AND',
                [
                    'key' => self::META_TOKEN_HASH,
                    'value' => $token_hash,
                    'compare' => '=',
                ],
                [
                    'key' => self::META_TOKEN_EXP,
                    'value' => $now,
                    'type' => 'NUMERIC',
                    'compare' => '>=',
                ],
            ],
        ]);

        if (!empty($users) && isset($users[0])) {
            return (int) $users[0];
        }

        return $user_id;
    }

    private static function get_user_favorite_ids(int $user_id): array {
        if ($user_id <= 0) {
            return [];
        }
        $raw = get_user_meta($user_id, self::META_FAVORITES_IDS, true);
        if (!is_array($raw)) {
            return [];
        }
        $normalized = [];
        foreach ($raw as $id) {
            $value = absint($id);
            if ($value > 0) {
                $normalized[$value] = true;
            }
        }
        $ids = array_keys($normalized);
        sort($ids, SORT_NUMERIC);
        return $ids;
    }

    private static function save_user_favorite_ids(int $user_id, array $ids): void {
        if ($user_id <= 0) {
            return;
        }
        $normalized = [];
        foreach ($ids as $id) {
            $value = absint($id);
            if ($value > 0) {
                $normalized[$value] = true;
            }
        }
        $final_ids = array_keys($normalized);
        sort($final_ids, SORT_NUMERIC);

        update_user_meta($user_id, self::META_FAVORITES_IDS, $final_ids);
        update_user_meta($user_id, self::META_FAVORITES_UPDATED_AT, time());
    }

    private static function get_user_favorites_payload(int $user_id): array {
        if ($user_id <= 0) {
            return [
                'ids' => [],
                'updated_at' => 0,
            ];
        }
        $ids = self::get_user_favorite_ids($user_id);
        $updated_at = (int) get_user_meta($user_id, self::META_FAVORITES_UPDATED_AT, true);
        if ($updated_at <= 0) {
            $updated_at = 0;
        }
        return [
            'ids' => $ids,
            'updated_at' => $updated_at,
        ];
    }

    public static function filter_order_pay_gateways($gateways) {
        if (is_admin() || !function_exists('is_checkout_pay_page') || !is_checkout_pay_page()) {
            return $gateways;
        }

        // Do not affect normal website order-pay behavior.
        // Restrict gateways only during mobile tokenized pay flow.
        if (!isset($_GET['mobile_pay_token'])) {
            return $gateways;
        }

        if (!is_array($gateways) || empty($gateways)) {
            return $gateways;
        }

        global $wp;
        $order_id = 0;

        if (is_object($wp) && isset($wp->query_vars['order-pay'])) {
            $order_id = absint($wp->query_vars['order-pay']);
        }

        if (!$order_id) {
            $order_id = self::get_request_order_pay_id();
        }

        if (!$order_id) {
            $order_key = '';
            if (is_object($wp) && isset($wp->query_vars['key'])) {
                $order_key = (string) $wp->query_vars['key'];
            }
            if ($order_key === '' && isset($_GET['key'])) {
                $order_key = wc_clean(wp_unslash($_GET['key']));
            }
            if ($order_key !== '') {
                $order_id = absint(wc_get_order_id_by_order_key($order_key));
            }
        }

        if (!$order_id) {
            return $gateways;
        }

        $order = wc_get_order($order_id);
        if (!($order instanceof WC_Order)) {
            return $gateways;
        }

        $selected_method = (string) $order->get_payment_method();
        if ($selected_method === '') {
            return [];
        }

        if (!isset($gateways[$selected_method])) {
            // Selected gateway is unavailable/misconfigured for this context.
            // Return only available gateways except known problematic pay-page rendering cases.
            return $gateways;
        }

        return [$selected_method => $gateways[$selected_method]];
    }

    public static function allow_order_pay_for_mobile_token($requires_login, $order) {
        $mobile_user_id = self::resolve_mobile_pay_user_from_request();
        if ($mobile_user_id <= 0) {
            return $requires_login;
        }

        if (!($order instanceof WC_Order)) {
            return $requires_login;
        }

        $order_customer_id = (int) $order->get_customer_id();
        if ($order_customer_id > 0 && $order_customer_id !== $mobile_user_id) {
            return $requires_login;
        }

        return false;
    }

    public static function handle_mobile_pay_autopay_entry(): void {
        if (!isset($_GET['autopay']) || (string) $_GET['autopay'] !== '1') {
            return;
        }
        if (!isset($_GET['mobile_pay_token'])) {
            return;
        }
        self::execute_mobile_autopay();
    }

    public static function handle_mobile_order_pay_auto_redirect(): void {
        if (!isset($_GET['autopay']) || (string) $_GET['autopay'] !== '1') {
            return;
        }
        if (!isset($_GET['mobile_pay_token'])) {
            return;
        }
        self::execute_mobile_autopay();
    }

    private static function execute_mobile_autopay(): void {
        $payload = self::get_mobile_pay_payload_from_request(false);
        if (!is_array($payload)) {
            self::mobile_pay_log('Autopay payload missing/invalid.');
            return;
        }

        $mobile_user_id = (int) ($payload['user_id'] ?? 0);
        if ($mobile_user_id > 0) {
            wp_set_current_user($mobile_user_id);
            wp_set_auth_cookie($mobile_user_id, true, is_ssl());
        }

        $order_id = (int) ($payload['order_id'] ?? 0);
        $order_key = (string) ($payload['order_key'] ?? '');
        if ($order_id <= 0) {
            self::mobile_pay_log('Autopay payload order_id is invalid.');
            return;
        }

        $order = wc_get_order($order_id);
        if (!($order instanceof WC_Order) && $order_key !== '') {
            $resolved_order_id = absint(wc_get_order_id_by_order_key($order_key));
            if ($resolved_order_id > 0) {
                $order_id = $resolved_order_id;
                $order = wc_get_order($order_id);
            }
        }
        if (!($order instanceof WC_Order)) {
            self::mobile_pay_log('Autopay order not found.', [
                'order_id' => $order_id,
                'order_key_suffix' => $order_key !== '' ? substr($order_key, -8) : '',
            ]);
            return;
        }

        if ($order->is_paid()) {
            $received_url = $order->get_checkout_order_received_url();
            if (is_string($received_url) && $received_url !== '') {
                wp_safe_redirect($received_url);
                exit;
            }
            return;
        }

        $gateway_redirect = self::resolve_gateway_redirect_url($order);
        if ($gateway_redirect !== '') {
            $token = isset($_GET['mobile_pay_token']) ? trim((string) wp_unslash($_GET['mobile_pay_token'])) : '';
            if ($token !== '') {
                delete_transient(self::MOBILE_PAY_TRANSIENT_PREFIX . $token);
            }
            self::set_mobile_return_order_cookie($order->get_id());
            wp_safe_redirect($gateway_redirect);
            exit;
        }

        // Final fallback: stay on order pay page without autopay params to avoid loop.
        $fallback_url = add_query_arg(
            [
                'pay_for_order' => 'true',
                'key' => (string) $order->get_order_key(),
            ],
            wc_get_checkout_url() . 'order-pay/' . $order_id . '/'
        );
        wp_safe_redirect($fallback_url);
        exit;
    }

    private static function extract_bearer_token(): string {
        $auth = '';

        if (isset($_SERVER['HTTP_AUTHORIZATION'])) {
            $auth = (string) $_SERVER['HTTP_AUTHORIZATION'];
        } elseif (function_exists('apache_request_headers')) {
            $headers = apache_request_headers();
            if (isset($headers['Authorization'])) {
                $auth = (string) $headers['Authorization'];
            }
        }

        if ($auth === '') {
            return '';
        }

        if (!preg_match('/^Bearer\s+(.*)$/i', $auth, $matches)) {
            return '';
        }

        return trim((string) ($matches[1] ?? ''));
    }

    private static function resolve_mobile_pay_user_from_request(): int {
        $payload = self::get_mobile_pay_payload_from_request(false);
        if (!is_array($payload)) {
            return 0;
        }
        return (int) ($payload['user_id'] ?? 0);
    }

    private static function get_mobile_pay_payload_from_request(bool $consume): ?array {
        $token = isset($_GET['mobile_pay_token']) ? trim((string) wp_unslash($_GET['mobile_pay_token'])) : '';
        if ($token === '') {
            return null;
        }

        $payload = get_transient(self::MOBILE_PAY_TRANSIENT_PREFIX . $token);
        if (!is_array($payload)) {
            return null;
        }

        $expires_at = (int) ($payload['expires_at'] ?? 0);
        if ($expires_at <= 0 || time() > $expires_at) {
            delete_transient(self::MOBILE_PAY_TRANSIENT_PREFIX . $token);
            return null;
        }

        $order_id = (int) ($payload['order_id'] ?? 0);
        $order_key = (string) ($payload['order_key'] ?? '');
        if ($order_id <= 0 || $order_key === '') {
            return null;
        }

        $query_order_id = self::get_request_order_pay_id();
        if ($query_order_id > 0 && $query_order_id !== $order_id) {
            return null;
        }

        $query_key = isset($_GET['key']) ? wc_clean(wp_unslash($_GET['key'])) : '';
        if ($query_key !== '' && $query_key !== $order_key) {
            return null;
        }

        if ($consume) {
            delete_transient(self::MOBILE_PAY_TRANSIENT_PREFIX . $token);
        }

        return $payload;
    }

    private static function get_request_order_pay_id(): int {
        global $wp;

        $order_id = 0;

        if (is_object($wp) && isset($wp->query_vars['order-pay'])) {
            $order_id = absint($wp->query_vars['order-pay']);
        }

        if (!$order_id && isset($_GET['order-pay'])) {
            $order_id = absint(wp_unslash($_GET['order-pay']));
        }

        if (!$order_id && isset($_SERVER['REQUEST_URI'])) {
            $path = (string) parse_url((string) wp_unslash($_SERVER['REQUEST_URI']), PHP_URL_PATH);
            if ($path !== '' && preg_match('~/order-pay/(\d+)(?:/|$)~', $path, $matches) === 1) {
                $order_id = absint($matches[1]);
            }
        }

        return $order_id;
    }

    private static function mark_order_for_mobile_return(WC_Order $order, string $app_scheme): void {
        $order->update_meta_data(self::MOBILE_RETURN_META, '1');
        $order->update_meta_data(self::MOBILE_RETURN_EXPIRES_META, (string) (time() + self::MOBILE_RETURN_TTL_SECONDS));
        $order->update_meta_data(self::MOBILE_RETURN_SCHEME_META, self::sanitize_app_scheme($app_scheme));
        $order->save();
    }

    private static function is_mobile_return_enabled_for_order(WC_Order $order): bool {
        $enabled = (string) $order->get_meta(self::MOBILE_RETURN_META, true);
        if ($enabled !== '1') {
            return false;
        }

        $expires_at = (int) $order->get_meta(self::MOBILE_RETURN_EXPIRES_META, true);
        if ($expires_at <= 0) {
            return true;
        }
        if ($expires_at < time()) {
            $order->delete_meta_data(self::MOBILE_RETURN_META);
            $order->delete_meta_data(self::MOBILE_RETURN_EXPIRES_META);
            $order->delete_meta_data(self::MOBILE_RETURN_SCHEME_META);
            $order->save();
            return false;
        }

        return true;
    }

    private static function build_mobile_return_proxy_url(int $order_id, string $order_key, string $status): string {
        return add_query_arg(
            [
                'order_id' => $order_id,
                'key' => $order_key,
                'status' => $status,
            ],
            rest_url('woo-mobile-auth/v1/payment_return')
        );
    }

    private static function get_mobile_return_scheme_for_order(WC_Order $order): string {
        $stored = (string) $order->get_meta(self::MOBILE_RETURN_SCHEME_META, true);
        $sanitized = self::sanitize_app_scheme($stored);
        if ($sanitized !== '') {
            return $sanitized;
        }
        return self::DEFAULT_MOBILE_APP_SCHEME;
    }

    private static function sanitize_app_scheme(string $scheme): string {
        $candidate = strtolower(trim($scheme));
        if ($candidate === '') {
            return '';
        }

        if (!preg_match('/^[a-z][a-z0-9+.-]{1,31}$/', $candidate)) {
            return '';
        }

        if ($candidate === 'http' || $candidate === 'https') {
            return '';
        }

        return $candidate;
    }

    private static function build_mobile_return_deeplink(int $order_id, string $status, string $app_scheme): string {
        $scheme = self::sanitize_app_scheme($app_scheme);
        if ($scheme === '') {
            $scheme = self::DEFAULT_MOBILE_APP_SCHEME;
        }

        return add_query_arg(
            [
                'order_id' => $order_id,
                'status' => $status,
            ],
            $scheme . '://payment-return'
        );
    }

    private static function normalize_mobile_payment_status(string $status, WC_Order $order): string {
        $normalized = strtolower(trim($status));

        if (in_array($normalized, ['success', 'paid', 'completed'], true)) {
            return 'success';
        }
        if (in_array($normalized, ['cancel', 'cancelled', 'canceled'], true)) {
            return 'canceled';
        }
        if (in_array($normalized, ['failed', 'failure', 'error'], true)) {
            return 'failed';
        }

        return $order->is_paid() ? 'success' : 'failed';
    }

    private static function resolve_mobile_return_status_from_request(WC_Order $order): string {
        if ($order->is_paid()) {
            return 'success';
        }

        $status_query = isset($_GET['status']) ? (string) wp_unslash($_GET['status']) : '';
        if ($status_query !== '') {
            return self::normalize_mobile_payment_status($status_query, $order);
        }

        if (isset($_GET['cancel_order']) || isset($_GET['cancel'])) {
            return 'canceled';
        }

        if (isset($_GET['order-pay']) || is_checkout_pay_page()) {
            return 'failed';
        }

        return 'failed';
    }

    private static function should_redirect_mobile_return_now(WC_Order $order): bool {
        if ($order->is_paid()) {
            return true;
        }

        if (isset($_GET['status']) || isset($_GET['cancel_order']) || isset($_GET['cancel'])) {
            return true;
        }

        if (function_exists('is_order_received_page') && is_order_received_page()) {
            return true;
        }

        $referer = isset($_SERVER['HTTP_REFERER']) ? strtolower((string) wp_unslash($_SERVER['HTTP_REFERER'])) : '';
        if (
            $referer !== '' &&
            (
                strpos($referer, 'tabby.ai') !== false ||
                strpos($referer, 'ziina') !== false
            )
        ) {
            return true;
        }

        return false;
    }

    private static function is_checkout_like_return_request(): bool {
        if (isset($_GET['key']) || isset($_GET['order_id']) || isset($_GET['order-pay'])) {
            return true;
        }

        if (function_exists('is_checkout') && is_checkout()) {
            $referer = isset($_SERVER['HTTP_REFERER']) ? strtolower((string) wp_unslash($_SERVER['HTTP_REFERER'])) : '';
            if (
                $referer !== '' &&
                (
                    strpos($referer, 'tabby.ai') !== false ||
                    strpos($referer, 'ziina') !== false
                )
            ) {
                return true;
            }
        }

        if (!isset($_SERVER['REQUEST_URI'])) {
            return false;
        }
        $path = (string) parse_url((string) wp_unslash($_SERVER['REQUEST_URI']), PHP_URL_PATH);

        return strpos($path, '/order-pay/') !== false || strpos($path, '/order-received/') !== false;
    }

    private static function get_order_from_request(int $order_id, string $order_key): ?WC_Order {
        if ($order_id > 0) {
            $order = wc_get_order($order_id);
            if ($order instanceof WC_Order) {
                return $order;
            }
        }

        $clean_order_key = wc_clean($order_key);
        if ($clean_order_key !== '') {
            $order_by_key_id = absint(wc_get_order_id_by_order_key($clean_order_key));
            if ($order_by_key_id > 0) {
                $order = wc_get_order($order_by_key_id);
                if ($order instanceof WC_Order) {
                    return $order;
                }
            }
        }

        return null;
    }

    private static function resolve_mobile_return_order(): ?WC_Order {
        $order_id = self::get_request_order_pay_id();
        $order_key = isset($_GET['key']) ? (string) wp_unslash($_GET['key']) : '';

        if (!$order_id && isset($_GET['order_id'])) {
            $order_id = absint(wp_unslash($_GET['order_id']));
        }

        if (!$order_id && function_exists('is_order_received_page') && is_order_received_page()) {
            global $wp;
            if (is_object($wp) && isset($wp->query_vars['order-received'])) {
                $order_id = absint($wp->query_vars['order-received']);
            }
        }

        $order = self::get_order_from_request($order_id, $order_key);
        if ($order instanceof WC_Order) {
            return $order;
        }

        $order_from_cookie = self::resolve_mobile_return_order_from_cookie();
        if ($order_from_cookie instanceof WC_Order) {
            return $order_from_cookie;
        }

        return self::resolve_latest_mobile_return_order_for_user();
    }

    private static function has_mobile_return_order_hint(): bool {
        return self::resolve_mobile_return_order_from_cookie() instanceof WC_Order;
    }

    private static function set_mobile_return_order_cookie(int $order_id): void {
        if ($order_id <= 0) {
            return;
        }

        $expires_at = time() + self::MOBILE_PAY_TOKEN_TTL_SECONDS;
        $secure = is_ssl();
        $value = (string) $order_id;

        if (PHP_VERSION_ID >= 70300) {
            setcookie(self::MOBILE_RETURN_ORDER_COOKIE, $value, [
                'expires' => $expires_at,
                'path' => '/',
                'secure' => $secure,
                'httponly' => true,
                'samesite' => 'Lax',
            ]);
        } else {
            $cookie_path = '/; samesite=Lax';
            setcookie(self::MOBILE_RETURN_ORDER_COOKIE, $value, $expires_at, $cookie_path, '', $secure, true);
        }

        $_COOKIE[self::MOBILE_RETURN_ORDER_COOKIE] = $value;
    }

    private static function clear_mobile_return_order_cookie(): void {
        $secure = is_ssl();

        if (PHP_VERSION_ID >= 70300) {
            setcookie(self::MOBILE_RETURN_ORDER_COOKIE, '', [
                'expires' => time() - HOUR_IN_SECONDS,
                'path' => '/',
                'secure' => $secure,
                'httponly' => true,
                'samesite' => 'Lax',
            ]);
        } else {
            $cookie_path = '/; samesite=Lax';
            setcookie(self::MOBILE_RETURN_ORDER_COOKIE, '', time() - HOUR_IN_SECONDS, $cookie_path, '', $secure, true);
        }

        unset($_COOKIE[self::MOBILE_RETURN_ORDER_COOKIE]);
    }

    private static function resolve_mobile_return_order_from_cookie(): ?WC_Order {
        if (!isset($_COOKIE[self::MOBILE_RETURN_ORDER_COOKIE])) {
            return null;
        }

        $order_id = absint(wp_unslash($_COOKIE[self::MOBILE_RETURN_ORDER_COOKIE]));
        if ($order_id <= 0) {
            return null;
        }

        $order = wc_get_order($order_id);
        if (!($order instanceof WC_Order)) {
            return null;
        }
        if (!self::is_mobile_return_enabled_for_order($order)) {
            return null;
        }
        return $order;
    }

    private static function resolve_latest_mobile_return_order_for_user(): ?WC_Order {
        $user_id = get_current_user_id();
        if ($user_id <= 0) {
            return null;
        }

        $orders = wc_get_orders([
            'customer_id' => $user_id,
            'limit' => 1,
            'orderby' => 'date',
            'order' => 'DESC',
            'status' => ['pending', 'on-hold', 'failed', 'processing', 'completed'],
            'meta_key' => self::MOBILE_RETURN_META,
            'meta_value' => '1',
        ]);

        if (!is_array($orders) || empty($orders)) {
            return null;
        }

        $candidate = $orders[0];
        if (!($candidate instanceof WC_Order)) {
            return null;
        }
        return self::is_mobile_return_enabled_for_order($candidate) ? $candidate : null;
    }

    private static function resolve_tabby_checkout_redirect($gateway, WC_Order $order): string {
        try {
            $gateway_class = get_class($gateway);
            $gateway_id = property_exists($gateway, 'id') ? (string) $gateway->id : '';
            $is_tabby = stripos($gateway_class, 'tabby') !== false || stripos($gateway_id, 'tabby') !== false;
            if (!$is_tabby) {
                return '';
            }

            if (!method_exists($gateway, 'getTabbyRedirectUrl')) {
                return '';
            }

            if (function_exists('WC') && WC() && WC()->session) {
                WC()->session->set('tabby_order_id', $order->get_id());
            }

            $resolver = \Closure::bind(
                static function ($targetOrder) {
                    return $this->getTabbyRedirectUrl($targetOrder);
                },
                $gateway,
                $gateway_class
            );

            if (!($resolver instanceof \Closure)) {
                self::mobile_pay_log('Tabby fallback resolver binding failed.', [
                    'order_id' => $order->get_id(),
                    'gateway_id' => $gateway_id,
                ]);
                return '';
            }

            $result = $resolver($order);
            if (is_wp_error($result)) {
                self::mobile_pay_log('Tabby fallback returned WP_Error.', [
                    'order_id' => $order->get_id(),
                    'gateway_id' => $gateway_id,
                    'error' => $result->get_error_message(),
                ]);
                return '';
            }

            $redirect = is_string($result) ? trim($result) : '';
            if ($redirect === '') {
                return '';
            }

            self::mobile_pay_log('Tabby fallback resolved redirect URL.', [
                'order_id' => $order->get_id(),
                'gateway_id' => $gateway_id,
            ]);
            return $redirect;
        } catch (\Throwable $e) {
            self::mobile_pay_log('Tabby fallback exception.', [
                'order_id' => $order->get_id(),
                'error' => $e->getMessage(),
            ]);
            return '';
        }
    }

    private static function resolve_gateway_redirect_url(WC_Order $order): string {
        $gateway_id = (string) $order->get_payment_method();
        if ($gateway_id === '') {
            self::mobile_pay_log('Gateway redirect resolve failed: empty payment method.', [
                'order_id' => $order->get_id(),
            ]);
            return '';
        }

        if (!function_exists('WC') || !WC() || !WC()->payment_gateways()) {
            self::mobile_pay_log('Gateway redirect resolve failed: payment gateway manager unavailable.', [
                'order_id' => $order->get_id(),
                'gateway_id' => $gateway_id,
            ]);
            return '';
        }

        $gateways = WC()->payment_gateways()->payment_gateways();
        if (!is_array($gateways) || empty($gateways)) {
            self::mobile_pay_log('Gateway redirect resolve failed: gateway list empty.', [
                'order_id' => $order->get_id(),
                'gateway_id' => $gateway_id,
            ]);
            return '';
        }

        $gateway = self::find_gateway_by_id($gateways, $gateway_id);
        if (!is_object($gateway)) {
            self::mobile_pay_log('Gateway redirect resolve failed: gateway object not found.', [
                'order_id' => $order->get_id(),
                'gateway_id' => $gateway_id,
            ]);
            return '';
        }

        $tabby_redirect = self::resolve_tabby_checkout_redirect($gateway, $order);
        if ($tabby_redirect !== '') {
            return $tabby_redirect;
        }

        if (!method_exists($gateway, 'process_payment')) {
            self::mobile_pay_log('Gateway redirect resolve failed: process_payment missing.', [
                'order_id' => $order->get_id(),
                'gateway_id' => $gateway_id,
            ]);
            return '';
        }

        $_POST['payment_method'] = $gateway_id;
        $_REQUEST['payment_method'] = $gateway_id;

        $result = $gateway->process_payment($order->get_id());
        if (is_array($result) && isset($result['result']) && $result['result'] === 'success') {
            $redirect = isset($result['redirect']) ? (string) $result['redirect'] : '';
            if ($redirect !== '') {
                return $redirect;
            }
        }

        self::mobile_pay_log('Gateway redirect resolve failed: process_payment did not return redirect.', [
            'order_id' => $order->get_id(),
            'gateway_id' => $gateway_id,
            'result_type' => gettype($result),
        ]);
        return '';
    }

    private static function find_gateway_by_id(array $gateways, string $gateway_id) {
        $gateway = $gateways[$gateway_id] ?? null;
        if (is_object($gateway)) {
            return $gateway;
        }

        foreach ($gateways as $candidate) {
            if (!is_object($candidate)) {
                continue;
            }
            if (property_exists($candidate, 'id') && (string) $candidate->id === $gateway_id) {
                return $candidate;
            }
        }

        return null;
    }

    private static function mobile_pay_log(string $message, array $context = []): void {
        $suffix = '';
        if (!empty($context)) {
            $encoded = wp_json_encode($context);
            if (is_string($encoded) && $encoded !== '') {
                $suffix = ' ' . $encoded;
            }
        }
        error_log('[woo-mobile-auth] ' . $message . $suffix);
    }

    private static function generate_token(): string {
        return rtrim(strtr(base64_encode(random_bytes(48)), '+/', '-_'), '=');
    }

    private static function generate_username_from_email(string $email): string {
        $base = sanitize_user(strstr($email, '@', true), true);
        if ($base === '') {
            $base = 'mobileuser';
        }
        return $base;
    }

    private static function generate_unique_username(string $base): string {
        $candidate = $base;
        $counter = 1;
        while (username_exists($candidate)) {
            $candidate = $base . $counter;
            $counter++;
        }
        return $candidate;
    }

    private static function store_user_token(int $user_id, string $token_hash, int $expires_at): void {
        if ($user_id <= 0 || $token_hash === '' || $expires_at <= 0) {
            return;
        }

        // Multi-device token storage: one meta key per token hash.
        update_user_meta(
            $user_id,
            self::TOKEN_META_PREFIX . $token_hash,
            $expires_at
        );

        // Keep legacy latest-token keys for backward compatibility.
        update_user_meta($user_id, self::META_TOKEN_HASH, $token_hash);
        update_user_meta($user_id, self::META_TOKEN_EXP, $expires_at);

        self::prune_user_tokens($user_id, time());
    }

    private static function find_user_id_by_token_hash(string $token_hash, int $now): int {
        if ($token_hash === '') {
            return 0;
        }

        $users = get_users([
            'number' => 1,
            'fields' => 'ID',
            'meta_query' => [
                [
                    'key' => self::TOKEN_META_PREFIX . $token_hash,
                    'value' => $now,
                    'type' => 'NUMERIC',
                    'compare' => '>=',
                ],
            ],
        ]);

        if (!empty($users) && isset($users[0])) {
            $user_id = (int) $users[0];
            self::prune_user_tokens($user_id, $now);
            return $user_id;
        }

        return 0;
    }

    private static function prune_user_tokens(int $user_id, int $now): void {
        if ($user_id <= 0) {
            return;
        }

        $all_meta = get_user_meta($user_id);
        if (!is_array($all_meta) || empty($all_meta)) {
            return;
        }

        $active = [];
        foreach ($all_meta as $meta_key => $values) {
            if (strpos((string) $meta_key, self::TOKEN_META_PREFIX) !== 0) {
                continue;
            }
            $expiry = isset($values[0]) ? (int) $values[0] : 0;
            if ($expiry <= $now) {
                delete_user_meta($user_id, (string) $meta_key);
                continue;
            }
            $active[(string) $meta_key] = $expiry;
        }

        if (count($active) <= self::MAX_ACTIVE_TOKENS_PER_USER) {
            return;
        }

        arsort($active, SORT_NUMERIC);
        $keys_to_keep = array_slice(array_keys($active), 0, self::MAX_ACTIVE_TOKENS_PER_USER, true);
        $keep_lookup = array_fill_keys($keys_to_keep, true);

        foreach ($active as $meta_key => $_expiry) {
            if (!isset($keep_lookup[$meta_key])) {
                delete_user_meta($user_id, $meta_key);
            }
        }
    }

    private static function normalize_otp_mode(string $mode): string {
        $normalized = strtolower(trim($mode));
        return $normalized === 'signup' ? 'signup' : 'reset';
    }

    private static function signup_email_key(string $email): string {
        return hash('sha256', strtolower(trim($email)));
    }

    private static function store_signup_otp(string $email, string $otp): void {
        $key = self::signup_email_key($email);
        set_transient(
            self::SIGNUP_OTP_TRANSIENT_PREFIX . $key,
            [
                'otp_hash' => wp_hash_password($otp),
                'expires_at' => time() + self::RESET_OTP_TTL_SECONDS,
            ],
            self::RESET_OTP_TTL_SECONDS
        );
        delete_transient(self::SIGNUP_OTP_VERIFIED_PREFIX . $key);
    }

    private static function validate_signup_otp(string $email, string $otp) {
        $key = self::signup_email_key($email);
        $payload = get_transient(self::SIGNUP_OTP_TRANSIENT_PREFIX . $key);
        if (!is_array($payload)) {
            return self::error_response('otp_not_found', 'No active OTP found. Please request a new code.', 404);
        }

        $otp_hash = isset($payload['otp_hash']) ? (string) $payload['otp_hash'] : '';
        $expires_at = isset($payload['expires_at']) ? (int) $payload['expires_at'] : 0;
        if ($otp_hash === '' || $expires_at <= 0) {
            self::clear_signup_otp($email);
            return self::error_response('otp_not_found', 'No active OTP found. Please request a new code.', 404);
        }
        if (time() > $expires_at) {
            self::clear_signup_otp($email);
            return self::error_response('otp_expired', 'Verification code expired. Please request a new code.', 410);
        }
        if (!wp_check_password($otp, $otp_hash)) {
            return self::error_response('otp_invalid', 'Invalid verification code.', 401);
        }
        return true;
    }

    private static function set_signup_verified(string $email): void {
        $key = self::signup_email_key($email);
        set_transient(
            self::SIGNUP_OTP_VERIFIED_PREFIX . $key,
            1,
            self::RESET_OTP_TTL_SECONDS
        );
    }

    private static function is_signup_email_verified(string $email): bool {
        $key = self::signup_email_key($email);
        return (int) get_transient(self::SIGNUP_OTP_VERIFIED_PREFIX . $key) === 1;
    }

    private static function clear_signup_otp(string $email): void {
        $key = self::signup_email_key($email);
        delete_transient(self::SIGNUP_OTP_TRANSIENT_PREFIX . $key);
    }

    private static function clear_signup_verified(string $email): void {
        $key = self::signup_email_key($email);
        delete_transient(self::SIGNUP_OTP_VERIFIED_PREFIX . $key);
    }

    private static function to_bool($value): bool {
        $normalized = strtolower(trim((string) $value));
        return in_array($normalized, ['1', 'true', 'yes', 'on'], true);
    }

    public static function get_user_first_name(array $user_arr): string {
        $user_id = isset($user_arr['id']) ? (int) $user_arr['id'] : 0;
        if ($user_id <= 0) {
            return '';
        }
        $first_name = (string) get_user_meta($user_id, 'first_name', true);
        return trim($first_name);
    }

    public static function get_user_last_name(array $user_arr): string {
        $user_id = isset($user_arr['id']) ? (int) $user_arr['id'] : 0;
        if ($user_id <= 0) {
            return '';
        }
        $last_name = (string) get_user_meta($user_id, 'last_name', true);
        return trim($last_name);
    }

    public static function get_user_email(array $user_arr): string {
        $user_id = isset($user_arr['id']) ? (int) $user_arr['id'] : 0;
        if ($user_id <= 0) {
            return '';
        }
        $user = get_user_by('id', $user_id);
        if (!($user instanceof WP_User)) {
            return '';
        }
        return (string) $user->user_email;
    }

    public static function get_user_phone_number(array $user_arr): string {
        $user_id = isset($user_arr['id']) ? (int) $user_arr['id'] : 0;
        if ($user_id <= 0) {
            return '';
        }
        $phone = (string) get_user_meta($user_id, 'phone_number', true);
        if ($phone === '') {
            $phone = (string) get_user_meta($user_id, 'billing_phone', true);
        }
        return trim($phone);
    }

    private static function validate_user_otp(int $user_id, string $otp) {
        $otp_hash = (string) get_user_meta($user_id, self::META_RESET_OTP_HASH, true);
        $expires_at = (int) get_user_meta($user_id, self::META_RESET_OTP_EXP, true);

        if ($otp_hash === '' || $expires_at <= 0) {
            return self::error_response('otp_not_found', 'No active OTP found. Please request a new code.', 404);
        }
        if (time() > $expires_at) {
            delete_user_meta($user_id, self::META_RESET_OTP_HASH);
            delete_user_meta($user_id, self::META_RESET_OTP_EXP);
            delete_user_meta($user_id, self::META_RESET_OTP_VERIFIED);
            return self::error_response('otp_expired', 'Verification code expired. Please request a new code.', 410);
        }
        if (!wp_check_password($otp, $otp_hash)) {
            return self::error_response('otp_invalid', 'Invalid verification code.', 401);
        }
        return true;
    }

    private static function validate_delete_otp(int $user_id, string $otp) {
        $otp_hash = (string) get_user_meta($user_id, self::META_DELETE_OTP_HASH, true);
        $expires_at = (int) get_user_meta($user_id, self::META_DELETE_OTP_EXP, true);

        if ($otp_hash === '' || $expires_at <= 0) {
            return self::error_response('otp_not_found', 'No active OTP found. Please request a new code.', 404);
        }
        if (time() > $expires_at) {
            delete_user_meta($user_id, self::META_DELETE_OTP_HASH);
            delete_user_meta($user_id, self::META_DELETE_OTP_EXP);
            return self::error_response('otp_expired', 'Verification code expired. Please request a new code.', 410);
        }
        if (!wp_check_password($otp, $otp_hash)) {
            return self::error_response('otp_invalid', 'Invalid verification code.', 401);
        }
        return true;
    }

    private static function anonymize_user_orders(int $user_id): void {
        if ($user_id <= 0 || !function_exists('wc_get_orders')) {
            return;
        }

        $orders = wc_get_orders([
            'customer_id' => $user_id,
            'limit' => -1,
            'return' => 'objects',
        ]);

        if (!is_array($orders) || empty($orders)) {
            return;
        }

        foreach ($orders as $order) {
            if (!($order instanceof WC_Order)) {
                continue;
            }

            $order->set_customer_id(0);

            $order->set_billing_first_name('');
            $order->set_billing_last_name('');
            $order->set_billing_company('');
            $order->set_billing_address_1('');
            $order->set_billing_address_2('');
            $order->set_billing_city('');
            $order->set_billing_state('');
            $order->set_billing_postcode('');
            $order->set_billing_country('');
            $order->set_billing_email('');
            $order->set_billing_phone('');

            $order->set_shipping_first_name('');
            $order->set_shipping_last_name('');
            $order->set_shipping_company('');
            $order->set_shipping_address_1('');
            $order->set_shipping_address_2('');
            $order->set_shipping_city('');
            $order->set_shipping_state('');
            $order->set_shipping_postcode('');
            $order->set_shipping_country('');

            $order->update_meta_data('_woo_mobile_deleted_user', '1');
            $order->save();
        }
    }

    private static function revoke_all_user_tokens(int $user_id): void {
        if ($user_id <= 0) {
            return;
        }
        $all_meta = get_user_meta($user_id);
        if (!is_array($all_meta)) {
            return;
        }
        foreach ($all_meta as $meta_key => $_values) {
            if (strpos((string) $meta_key, self::TOKEN_META_PREFIX) === 0) {
                delete_user_meta($user_id, (string) $meta_key);
            }
        }
        delete_user_meta($user_id, self::META_TOKEN_HASH);
        delete_user_meta($user_id, self::META_TOKEN_EXP);
    }

    private static function clear_user_sensitive_meta(int $user_id): void {
        if ($user_id <= 0) {
            return;
        }
        delete_user_meta($user_id, self::META_RESET_OTP_HASH);
        delete_user_meta($user_id, self::META_RESET_OTP_EXP);
        delete_user_meta($user_id, self::META_RESET_OTP_VERIFIED);
        delete_user_meta($user_id, self::META_DELETE_OTP_HASH);
        delete_user_meta($user_id, self::META_DELETE_OTP_EXP);
        delete_user_meta($user_id, self::META_FAVORITES_IDS);
        delete_user_meta($user_id, self::META_FAVORITES_UPDATED_AT);
    }

    private static function resolve_reassign_user_id(int $deleted_user_id): int {
        $admins = get_users([
            'role' => 'administrator',
            'number' => 1,
            'fields' => 'ID',
            'exclude' => [$deleted_user_id],
        ]);
        if (is_array($admins) && isset($admins[0])) {
            return (int) $admins[0];
        }
        return 0;
    }

    private static function error_response(string $code, string $message, int $status): WP_REST_Response {
        return new WP_REST_Response([
            'success' => false,
            'data' => [
                'code' => $code,
                'msg' => $message,
            ],
        ], $status);
    }
}

Woo_Mobile_Auth_Login_Endpoint::init();
