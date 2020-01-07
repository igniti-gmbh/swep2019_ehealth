import datetime

import pandas as pd
import plotly.graph_objs as go
from dash.dependencies import Input, Output
from flask import session

from .callback_functions import has_cookie_access
from ..python_firebase.firestore_connect import store


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
            dic = store.document('users', session['uid']).get().to_dict()
            return dic['steps_today_total']

    @dashapp.callback(
        Output("step_goal", "children"),
        [Input('interval-component', 'n_intervals')])
    def reload_steps_goal(n):
        if session['uid'] is None:
            return '/'
        else:
            dic = store.document('users', session['uid']).get().to_dict()
            goal_reached = dic['steps_today_total'] / dic['daily_step_goal'] * 100

            if goal_reached > 100:
                goal_reached = 100

            return str(goal_reached) + '%'

    @dashapp.callback(
        Output("displayName", "children"),
        [Input('interval-component', 'n_intervals')])
    def show_display_name(n):
        if session['uid'] is None:
            return '/'
        else:
            if session['name']:
                return session['name']
            return '/'

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
                dic = store.document('users', session['uid']).get().to_dict()
                return dic['age']

    @dashapp.callback(
        Output("count_graph", "figure"),
        [Input('interval-component', 'n_intervals')], )
    def get_last_day(n):

        uid = session['uid']

        if uid is None:
            return False
        else:
            values = []
            hours = []

            now = datetime.datetime.now()

            for i in range(0, 23):
                docRef = store.document(
                    'users/' + str(uid) + '/' + str(now.year) + '/' + str(now.month) + '/' + str(now.day) + '/'
                    + str(now.hour))

                docSnap = docRef.get()

                hours.append(now.hour)

                if docSnap.exists:
                    docDic = docSnap.to_dict()
                    values.append(str(docDic['value']))
                else:
                    values.append(str(0))

                now = now - datetime.timedelta(hours=1)

            layout = dict(autosize=True,
                          margin=dict(l=30, r=30, b=20, t=40),
                          hovermode="closest",
                          plot_bgcolor="#F9F9F9",
                          paper_bgcolor="#F9F9F9",
                          legend=dict(font=dict(size=10), orientation="h"),
                          )

            data = [go.Bar(
                x=hours,
                y=values
            )]

            fig = go.Figure(data=data, layout=layout)
            fig.update_yaxes(rangemode="nonnegative")
            return fig

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
