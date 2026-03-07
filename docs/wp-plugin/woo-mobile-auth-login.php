<?php
/**
 * Plugin Name: Woo Mobile Auth Login Endpoint
 * Description: Adds /wp-json/woo-mobile-auth/v1/login_user endpoint and Bearer-token auth compatibility for mobile apps.
 * Version: 1.0.0
 * Author: Solutionium
 */

if (!defined('ABSPATH')) {
    exit;
}

final class Woo_Mobile_Auth_Login_Endpoint {
    private const META_TOKEN_HASH = '_woo_mobile_auth_token_hash';
    private const META_TOKEN_EXP = '_woo_mobile_auth_token_exp';
    private const TOKEN_TTL_SECONDS = 30 * DAY_IN_SECONDS;

    public static function init(): void {
        add_action('rest_api_init', [self::class, 'register_routes']);
        add_filter('determine_current_user', [self::class, 'authenticate_bearer_token'], 20);
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
    }

    public static function login_user(WP_REST_Request $request): WP_REST_Response {
        $username = (string) $request->get_param('user');
        $password = (string) $request->get_param('password');

        if ($username === '' || $password === '') {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'empty_credentials',
                    'msg' => 'Username and password are required.',
                ],
            ], 400);
        }

        $user = wp_authenticate($username, $password);

        if (is_wp_error($user)) {
            return new WP_REST_Response([
                'success' => false,
                'data' => [
                    'code' => 'invalid_credentials',
                    'msg' => $user->get_error_message() ?: 'Invalid username or password.',
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
}

Woo_Mobile_Auth_Login_Endpoint::init();
