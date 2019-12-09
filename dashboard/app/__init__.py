from dash import dash
from flask import Flask
import os
from config import BaseConfig


def create_app():
    server = Flask(__name__)
    server.config.from_object(BaseConfig)

    register_blueprints(server)
    register_dashapps(server)

    return server


def register_blueprints(server):
    from app.webapp import server_bp

    server.register_blueprint(server_bp)


def register_dashapps(app):
    from app.dashapp.layout import layout
    from app.dashapp.callbacks import register_callbacks

    # Meta tags for viewport responsiveness
    meta_viewport = {"name": "viewport", "content": "width=device-width, initial-scale=1, shrink-to-fit=no"}

    dashapp = dash.Dash(__name__,
                        server=app,
                        url_base_pathname='/dashboard/',
                        assets_url_path=os.path.join(app.root_path, 'dashapp', 'assets'),
                        assets_folder=os.path.join(app.root_path, 'dashapp', 'assets'),
                        meta_tags=[meta_viewport])

    with app.app_context():
        dashapp.title = 'Ignite E-Health'
        dashapp.layout = layout
        register_callbacks(dashapp)


def _protect_dashviews(dashapp):
    for view_func in dashapp.server.view_functions:
        if view_func.startswith(dashapp.config.url_base_pathname):
            dashapp.server.view_functions[view_func] = login_required(dashapp.server.view_functions[view_func])
