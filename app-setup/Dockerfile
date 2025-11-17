FROM chatwoot/chatwoot:latest

# -----------------------------
# Override Chatwoot Initializers
# -----------------------------
RUN mkdir -p /app/config/initializers && \
    \
    # Session cookie configuration
    printf "%s\n" \
"Rails.application.config.session_store :cookie_store," \
"  key: '_chatwoot_session'," \
"  domain: ENV.fetch('COOKIE_DOMAIN', '.traloitudong.com')," \
"  same_site: :none," \
"  secure: true" \
> /app/config/initializers/session_store.rb && \
    \
    # WebSocket allowed origins
    printf "%s\n" \
"Rails.application.config.action_cable.allowed_request_origins = [" \
"  ENV.fetch('FRONTEND_URL', 'https://traloitudong.com')," \
"  'https://admin.traloitudong.com'," \
"  'https://chatwoot.traloitudong.com'" \
"]" \
> /app/config/initializers/action_cable_origins.rb

# Ensure puma directory exists
RUN mkdir -p /app/tmp/pids
