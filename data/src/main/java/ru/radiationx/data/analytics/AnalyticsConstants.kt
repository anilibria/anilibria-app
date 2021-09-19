package ru.radiationx.data.analytics

object AnalyticsConstants {

    /* Shared/Screens */
    const val screen_main = "screen_main"
    const val screen_fast_search = "screen_fast_search"
    const val screen_feed = "screen_feed"
    const val screen_schedule = "screen_schedule"
    const val screen_release = "screen_release"
    const val screen_release_comments = "screen_release_comments"
    const val screen_player = "screen_player"
    const val screen_web_player = "screen_web_player"
    const val screen_favorites = "screen_favorites"
    const val screen_catalog = "screen_catalog"
    const val screen_catalog_filter = "screen_catalog_filter"
    const val screen_youtube = "screen_youtube"
    const val screen_other = "screen_other"
    const val screen_history = "screen_history"
    const val screen_page = "screen_page"
    const val screen_settings = "screen_settings"
    const val screen_update = "screen_update"
    const val screen_configuration = "screen_configuration"
    const val screen_auth_device = "screen_auth_device"
    const val screen_auth_main = "screen_auth_main"
    const val screen_auth_vk = "screen_auth_vk"
    const val screen_auth_social = "screen_auth_social"
    const val screen_donation_detail = "screen_donation_detail"
    const val screen_donation_yoomoney = "screen_donation_yoomoney"
    const val screen_donation_jointeam = "screen_donation_jointeam"
    const val screen_donation_infra = "screen_donation_infra"
    const val notification_local_update = "notification_local_update"
    const val notification_push_update = "notification_push_update"
    const val link_router = "link_router"


    /* App */
    const val app_time_to_create = "app_time_to_create"
    const val app_time_to_init = "app_time_to_init"
    const val app_time_to_activity = "app_time_to_activity"

    /* Configuring */
    const val config_open = "config_open"
    const val config_check_full = "config_check_full"
    const val config_check_last = "config_check_last"
    const val config_load_config = "config_load_config"
    const val config_check_avail = "config_check_avail"
    const val config_check_proxies = "config_check_proxies"
    const val config_repeat = "config_repeat"
    const val config_skip = "config_skip"
    const val config_next = "config_next"

    /* Fast Search */
    const val fast_search_open = "fast_search_open"
    const val fast_search_release_click = "fast_search_release_click"
    const val fast_search_catalog_click = "fast_search_catalog_click"
    const val fast_search_google_click = "fast_search_google_click"

    /* Schedule */
    const val schedule_open = "schedule_open"
    const val schedule_horizontal_scroll = "schedule_horizontal_scroll"
    const val schedule_release_click = "schedule_release_click"

    /* Feed */
    const val feed_open = "feed_open"
    const val feed_schedule_click = "feed_schedule_click"
    const val feed_schedule_horizontal_scroll = "feed_schedule_horizontal_scroll"
    const val feed_schedule_release_click = "feed_schedule_release_click"
    const val feed_release_click = "feed_release_click"
    const val feed_youtube_click = "feed_youtube_click"
    const val feed_random_click = "feed_random_click"
    const val feed_load_page = "feed_load_page"

    /* Release */
    const val release_open = "release_open"
    const val release_copy = "release_copy"
    const val release_share = "release_share"
    const val release_shortcut = "release_shortcut"
    const val release_history_reset = "release_history_reset"
    const val release_history_view_all = "release_history_view_all"
    const val release_history_reset_episode = "release_history_reset_episode"
    const val release_episodes_top_start = "release_episodes_top_start"
    const val release_episodes_top_continue = "release_episodes_top_continue"
    const val release_episodes_start = "release_episodes_start"
    const val release_episodes_continue = "release_episodes_continue"
    const val release_episode_play = "release_episode_play"
    const val release_episode_external = "release_episode_external"
    const val release_episode_download = "release_episode_download"
    const val release_episode_download_url = "release_episode_download_url"
    const val release_web_player = "release_web_player"
    const val release_torrent = "release_torrent"
    const val release_donate = "release_donate"
    const val release_description_expand = "release_description_expand"
    const val release_description_link = "release_description_link"
    const val release_schedule_click = "release_schedule_click"
    const val release_genre_click = "release_genre_click"
    const val release_favorite_add = "release_favorite_add"
    const val release_favorite_remove = "release_favorite_remove"
    const val release_comments_click = "release_comments_click"
    const val release_episodes_tab_click = "release_episodes_tab_click"

    /* Youtube Videos */
    const val youtube_videos_open = "youtube_videos_open"
    const val youtube_videos_video_click = "youtube_videos_video_click"
    const val youtube_videos_load_page = "youtube_videos_load_page"

    /* Youtube (external app) */
    const val youtube_video_open = "youtube_video_open"

    /* Catalog */
    const val catalog_open = "catalog_open"
    const val catalog_release_click = "catalog_release_click"
    const val catalog_fast_search_click = "catalog_fast_search_click"
    const val catalog_on_filter_click = "catalog_on_filter_click"
    const val catalog_load_page = "catalog_load_page"

    /* Catalog Filter */
    const val catalog_filter_open = "catalog_filter_open"
    const val catalog_filter_use_time = "catalog_filter_use_time"
    const val catalog_filter_apply_click = "catalog_filter_apply_click"

    /* Comments */
    const val comments_open = "comments_open"
    const val comments_loaded = "comments_loaded"
    const val comments_error = "comments_error"

    /* Favorites */
    const val favorites_open = "favorites_open"
    const val favorites_search_click = "favorites_search_click"
    const val favorites_search_release_click = "favorites_search_release_click"
    const val favorites_release_click = "favorites_release_click"
    const val favorites_delete_click = "favorites_delete_click"
    const val favorites_load_page = "favorites_load_page"

    /* Other */
    const val other_open = "other_open"
    const val other_login_click = "other_login_click"
    const val other_logout_click = "other_logout_click"
    const val other_profile_click = "other_profile_click"
    const val other_history_click = "other_history_click"
    const val other_team_click = "other_team_click"
    const val other_donate_click = "other_donate_click"
    const val other_auth_device_click = "other_auth_device_click"
    const val other_settings_click = "other_settings_click"
    const val other_link_click = "other_link_click"

    /* History */
    const val history_open = "history_open"
    const val history_search_click = "history_search_click"
    const val history_search_release_click = "history_search_release_click"
    const val history_release_click = "history_release_click"
    const val history_release_delete_click = "history_release_delete_click"

    /* Settings */
    const val settings_open = "settings_open"
    const val settings_notification_main_change = "settings_notification_main_change"
    const val settings_notification_system_change = "settings_notification_system_change"
    const val settings_theme_change = "settings_theme_change"
    const val settings_reverse_order_change = "settings_reverse_order_change"
    const val settings_quality_click = "settings_quality_click"
    const val settings_quality_change = "settings_quality_change"
    const val settings_player_click = "settings_player_click"
    const val settings_player_change = "settings_player_change"
    const val settings_check_updates_click = "settings_check_updates_click"
    const val settings_other_apps_click = "settings_other_apps_click"
    const val settings_4pda_click = "settings_4pda_click"

    /* Auth Device */
    const val auth_device_open = "auth_device_open"
    const val auth_device_error = "auth_device_error"
    const val auth_device_success = "auth_device_success"
    const val auth_device_use_time = "auth_device_use_time"

    /* Auth Main */
    const val auth_main_open = "auth_main_open"
    const val auth_main_social_click = "auth_main_social_click"
    const val auth_main_reg_click = "auth_main_reg_click"
    const val auth_main_reg_to_site_click = "auth_main_reg_to_site_click"
    const val auth_main_skip_click = "auth_main_skip_click"
    const val auth_main_login_click = "auth_main_login_click"
    const val auth_main_error = "auth_main_error"
    const val auth_main_success = "auth_main_success"
    const val auth_main_wrong_success = "auth_main_wrong_success"
    const val auth_main_use_time = "auth_main_use_time"

    /* Auth Social */
    const val auth_social_open = "auth_social_open"
    const val auth_social_error = "auth_social_error"
    const val auth_social_success = "auth_social_success"
    const val auth_social_use_time = "auth_social_use_time"

    /* Auth VK */
    const val auth_vk_open = "auth_vk_open"
    const val auth_vk_error = "auth_vk_error"
    const val auth_vk_success = "auth_vk_success"
    const val auth_vk_use_time = "auth_vk_use_time"

    /* Player */
    const val player_open = "player_open"
    const val player_time_to_start = "player_time_to_start"
    const val player_buffering_time = "player_load_time"
    const val player_error = "player_error"
    const val player_use_time = "player_use_time"
    const val player_play_click = "player_play_click"
    const val player_pause_click = "player_pause_click"
    const val player_prev_click = "player_prev_click"
    const val player_next_click = "player_next_click"
    const val player_rewind_slide = "player_rewind_slide"
    const val player_rewind_double_tap = "player_rewind_double_tap"
    const val player_rewind_seek = "player_rewind_seek"
    const val player_fullscreen = "player_fullscreen"
    const val player_pip = "player_pip"
    const val player_settings_click = "player_settings_click"
    const val player_settings_quality_click = "player_settings_quality_click"
    const val player_settings_speed_click = "player_settings_speed_click"
    const val player_settings_scale_click = "player_settings_scale_click"
    const val player_settings_pip_click = "player_settings_pip_click"
    const val player_settings_quality_change = "player_settings_quality_change"
    const val player_settings_speed_change = "player_settings_speed_change"
    const val player_settings_scale_change = "player_settings_scale_change"
    const val player_settings_pip_change = "player_settings_pip_change"
    const val player_episodes_finish = "player_episodes_finish"
    const val player_episodes_finish_action = "player_episodes_finish_action"
    const val player_season_finish = "player_season_finish"
    const val player_season_finish_action = "player_season_finish_action"

    /* Web Player */
    const val web_player_open = "web_player_open"
    const val web_player_loaded = "web_player_loaded"
    const val web_player_error = "web_player_error"
    const val web_player_use_time = "web_player_use_time"

    /* Updater */
    const val updater_open = "updater_open"
    const val updater_download_click = "updater_download_click"
    const val updater_source_download = "updater_source_download"
    const val updater_use_time = "updater_use_time"
    const val app_update_card = "app_update_card"
    const val app_update_card_click = "app_update_card_click"
    const val app_update_card_close = "app_update_card_close"

    /* Static Page */
    const val page_open = "page_open"
    const val page_loaded = "page_loaded"
    const val page_error = "page_error"
    const val page_use_time = "page_use_time"

    /* Donation */
    const val donation_detail_open = "donation_detail_open"
    const val donation_detail_link_click = "donation_detail_link_click"
    const val donation_detail_patreon_click = "donation_detail_patreon_click"
    const val donation_detail_yoomoney_click = "donation_detail_yoomoney_click"
    const val donation_detail_donationalerts_click = "donation_detail_donationalerts_click"
    const val donation_detail_jointeam_click = "donation_detail_jointeam_click"
    const val donation_detail_infra_click = "donation_detail_infra_click"

    const val donation_yoomoney_open = "donation_yoomoney_open"
    const val donation_yoomoney_help_click = "donation_yoomoney_help_click"
    const val donation_yoomoney_accept_click = "donation_yoomoney_accept_click"

    const val donation_jointeam_open = "donation_jointeam_open"
    const val donation_jointeam_link_click = "donation_jointeam_link_click"
    const val donation_jointeam_notice_click = "donation_jointeam_notice_click"
    const val donation_jointeam_telegram_click = "donation_jointeam_telegram_click"

    const val donation_infra_open = "donation_infra_open"
    const val donation_infra_link_click = "donation_infra_link_click"
    const val donation_infra_telegram_click = "donation_infra_telegram_click"

    /* Donation Card */
    const val donation_card_new_click = ""
    const val donation_card_new_close_click = ""

    /*
    * Глобально добавить в профиль пользователя текущий adresss
    * Глобально добавить в профиль темную тему
    * Глобально добавить состояние авторизации
    * и т.д.
    * Евевнт времени запуска аппки
    * */
}