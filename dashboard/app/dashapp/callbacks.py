from dash.dependencies import Input, Output, ClientsideFunction
import copy
import dash
from dash.exceptions import PreventUpdate
from .callback_functions import has_cookie_access
import pandas as pd
from flask import session


def register_callbacks(dashapp):

    @dashapp.callback(
        Output("connection_status", "children"),
        [Input('interval-component', 'n_intervals')])
    def show_status(n):
        if has_cookie_access():
            return "Connected"
        else:
            return "Disconnected"

    @dashapp.callback(
        Output("current_date", "children"),
        [Input('interval-component', 'n_intervals')])
    def show_date(n):
        return pd.Timestamp.now().date().strftime('%d.%m.%Y')

    @dashapp.callback(
        Output("current_time", "children"),
        [Input('interval-component', 'n_intervals')])
    def show_date(n):
        return pd.Timestamp.now().time().strftime('%H:%M')

    @dashapp.callback(
        Output("stepsToday", "children"),
        [Input('interval-component', 'n_intervals')])
    def reload_steps_today(n):

        decode_claims = has_cookie_access()

        if session['uid'] is None:
            return "/"
        else:
            return session['steps_today_total']

    @dashapp.callback(
        Output("step_goal", "children"),
        [Input('interval-component', 'n_intervals')])
    def reload_steps_goal(n):
        if session['uid'] is None:
            return '/'
        else:
            goal_reached = session['steps_today_total'] / session['daily_step_goal']
            return str(goal_reached) + '%'


    @dashapp.callback(
        Output("displayName", "children"),
        [Input('interval-component', 'n_intervals')])
    def show_displayName(n):
        if session['uid'] is None:
            return '/'
        else:
            return session['name']

    @dashapp.callback(
        Output("room", "children"),
        [Input('interval-component', 'n_intervals')])
    def show_room(n):
        if session['uid'] is None:
            return '/'
        else:
            if session['room'] is None:
                return '/'
            else:
                return session['room']

    @dashapp.callback(
        Output("age", "children"),
        [Input('interval-component', 'n_intervals')])
    def get_age(n):
        if session['uid'] is None:
            return '/'
        else:
            if session['age'] is None:
                return '/'
            else:
                return session['age']

    # # @dashapp.callback(
    # #     Output("count_graph", "figure"),
    # #     [Input('interval-component', 'n_intervals')])
    # # def make_count_figure(n):
    # #     layout_count = copy.deepcopy(layout)
    # #
    # #     g = df[["DATE", "STEPS"]]
    # #     g = df[(g['DATE'] <= DateToday)]
    # #     g.index = g["DATE"]
    # #
    # #     data = [
    # #         dict(
    # #             type="bar",
    # #             x=g.index,
    # #             y=g["STEPS"],
    # #             name="Schritte",
    # #             showlegend="false",
    # #         ),
    # #     ]
    # #
    # #     layout_count["title"] = "Schritte"
    # #     layout_count["dragmode"] = "select"
    # #     layout_count["showlegend"] = False
    # #     layout_count["autosize"] = True
    # #
    # #     figure = dict(data=data, layout=layout_count)
    # #     return figure
    # #
    # #
    # # @dashapp.callback(
    # #     Output("stepgoal_graph", "figure"),
    # #     [Input('interval-component', 'n_intervals'),
    # #      Input('stepsToday', "children")])
    # # def make_stepgoal_figure(n, steps):
    # #     layout_count = copy.deepcopy(layout)
    # #     goal = 10000
    # #
    # #     data = [
    # #         dict(
    # #             type="indicator",
    # #             mode="number+delta",
    # #             value=steps,
    # #             domain={'x': [0, 0.5], 'y': [0, 0.5]},
    # #             delta={'reference': goal, 'relative': True, 'position': "top"}
    # #         ),
    # #     ]
    # #
    # #     layout_count["title"] = "Anderer Graph"
    # #     layout_count["dragmode"] = "select"
    # #     layout_count["showlegend"] = False
    # #     layout_count["autosize"] = True
    # #
    # #     figure = dict(data=data, layout=layout_count)
    # #     return figure
