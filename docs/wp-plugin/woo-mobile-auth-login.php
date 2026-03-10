<?php
/**
 * Plugin Name: Solu Mobile Auth Login Endpoint
 * Description: Adds /wp-json/woo-mobile-auth/v1/login_user endpoint and Bearer-token auth compatibility for mobile apps.
 * Version: 1.0.6
 * Author: Solutionium
 */

if (!defined('ABSPATH')) {
    exit;
}

final class Woo_Mobile_Auth_Login_Endpoint {
    private const META_TOKEN_HASH = '_woo_mobile_auth_token_hash';
    private const META_TOKEN_EXP = '_woo_mobile_auth_token_exp';
    private const META_RESET_OTP_HASH = '_woo_mobile_auth_reset_otp_hash';
    private const META_RESET_OTP_EXP = '_woo_mobile_auth_reset_otp_exp';
    private const META_RESET_OTP_VERIFIED = '_woo_mobile_auth_reset_otp_verified';
    private const TOKEN_TTL_SECONDS = 30 * DAY_IN_SECONDS;
    private const RESET_OTP_TTL_SECONDS = 10 * MINUTE_IN_SECONDS;

    public static function init(): void {
        add_action('rest_api_init', [self::class, 'register_routes']);
        add_action('rest_api_init', [self::class, 'register_user_rest_fields']);
        add_filter('determine_current_user', [self::class, 'authenticate_bearer_token'], 20);
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
                    'required' => true,
                    'type' => 'string',
                ],
                'password' => [
                    'required' => true,
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

        update_user_meta($user->ID, self::META_TOKEN_HASH, $token_hash);
        update_user_meta($user->ID, self::META_TOKEN_EXP, $expires_at);

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

        if ($name === '' || $email === '' || $phone === '' || $password === '') {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'missing_fields',
                    'msg' => 'Name, email, phone and password are required.',
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
        update_user_meta($user_id, 'billing_phone', $phone);

        $token = self::generate_token();
        $token_hash = hash('sha256', $token);
        $expires_at = time() + self::TOKEN_TTL_SECONDS;

        update_user_meta($user_id, self::META_TOKEN_HASH, $token_hash);
        update_user_meta($user_id, self::META_TOKEN_EXP, $expires_at);

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
        if (!is_email($email)) {
            return self::error_response('invalid_email', 'Please provide a valid email address.', 400);
        }

        $user = get_user_by('email', $email);
        if (!($user instanceof WP_User)) {
            return self::error_response('user_not_found', 'No account found with this email.', 404);
        }

        $otp = (string) random_int(100000, 999999);
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

        if (!is_email($email) || $otp === '') {
            return self::error_response('invalid_request', 'Email and OTP are required.', 400);
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

    public static function authenticate_bearer_token($user_id) {
        if (!empty($user_id)) {
            return $user_id;
        }

        $token = self::extract_bearer_token();
        if ($token === '') {
            return $user_id;
        }

        $token_hash = hash('sha256', $token);
        $now = time();

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
