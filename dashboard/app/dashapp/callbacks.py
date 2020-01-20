import datetime
import json

import pandas as pd
import plotly.graph_objs as go
from dash.dependencies import Input, Output
from flask import session

from .callback_functions import has_cookie_access
from ..python_firebase.firestore_connect import store


def register_callbacks(dashapp):
    @dashapp.callback(
        Output('intermediate-value', 'children'),
        [Input('date-picker', 'date'), Input('interval-component', 'n_intervals')], )
    def clean_data(date, n):

        date = datetime.datetime.strptime(date.split(' ')[0], '%Y-%m-%d')
        df = getValuesFromFirebase(date)
        graphDf = createDataframe(df)
        totalSteps = dfTotal(graphDf)
        accountInfos = getAccInfos()



        datasets = {
            'df': graphDf.to_json(orient='split', date_format='iso'),
            'totalSteps': totalSteps,
            'accountInfos': accountInfos,
        }

        return json.dumps(datasets)

    @dashapp.callback(
        Output("connection_status", "children"),
        [Input('intermediate-value', 'children')])
    def show_status(n):
        if has_cookie_access():
            return "Connected"
        else:
            return "Disconnected"

    @dashapp.callback(
        Output("current_date", "children"),
        [Input('intermediate-value', 'children')])
    def show_date(n):
        return pd.Timestamp.now().date().strftime('%d.%m.%Y')

    @dashapp.callback(
        Output("current_time", "children"),
        [Input('intermediate-value', 'children')])
    def show_date(n):
        return pd.Timestamp.now().time().strftime('%H:%M')

    @dashapp.callback(
        Output("step_goal", "children"),
        [Input('intermediate-value', 'children')])
    def reload_steps_goal(json_data):
        data = json.loads(json_data)
        total_steps = data['totalSteps']
        step_goal = data['accountInfos']['daily_step_goal']

        goal_reached = total_steps / int(step_goal) * 100

        if goal_reached > 100:
            goal_reached = 100

        return str(goal_reached) + '%'

    @dashapp.callback(
        Output("displayName", "children"),
        [Input('intermediate-value', 'children')])
    def show_display_name(json_data):
        data = json.loads(json_data)
        total_steps = data['totalSteps']
        name = data['accountInfos']['name']

        if name is None:
            return '/'
        else:
            return name

    @dashapp.callback(
        Output("room", "children"),
        [Input('intermediate-value', 'children')])
    def show_room(json_data):
        data = json.loads(json_data)
        room = data['accountInfos']['room']

        if room is None:
            return '/'
        else:
            return str(room)

    @dashapp.callback(
        Output("age", "children"),
        [Input('intermediate-value', 'children')])
    def show_age(json_data):
        data = json.loads(json_data)
        age = data['accountInfos']['age']

        if age is None:
            return '/'
        else:
            return str(age)

    @dashapp.callback(
        Output("stepsToday", "children"),
        [Input('intermediate-value', 'children')])
    def reload_steps_today(json_data):
        data = json.loads(json_data)

        return data['totalSteps']

    @dashapp.callback(
        Output("count_graph", "figure"),
        [Input('intermediate-value', 'children')])
    def show_graph(json_data):

        data = json.loads(json_data)
        df = pd.read_json(data['df'], orient='split')

        layout = dict(autosize=True,
                      margin=dict(l=30, r=30, b=20, t=40),
                      hovermode="closest",
                      plot_bgcolor="#F9F9F9",
                      paper_bgcolor="#F9F9F9",
                      legend=dict(font=dict(size=10), orientation="h"),
                      xaxis=dict(type='category', title='hour'),
                      yaxis=dict(title='steps', rangemode='nonnegative')
                      )

        data = [go.Bar(
            x=df['hour'],
            y=df['value'],
        )]

        fig = go.Figure(data=data, layout=layout)
        return fig


def getValuesFromFirebase(date):
    range_start = 23
    range_end = -1
    range_iteration = -1

    date = date.replace(hour=23, minute=59, second=59)

    # dataframe aus dem graph erstellt wird
    df = pd.DataFrame(columns=['hour', 'value'])

    day = date.day
    colRef = store.collection(
        'users/' + str(session['uid']) + '/' + str(date.year) + '/' + str(date.month) + '/'
        + str(day))
    snapshot = colRef.list_documents()

    for doc in snapshot:
        value = None
        docID = doc.id

        for i in range(range_start, range_end, range_iteration):

            iterated_hour = date - datetime.timedelta(hours=i)

            if int(docID) == iterated_hour.hour:
                df_new = pd.DataFrame({
                    'hour': [iterated_hour.hour],
                    'value': doc.get().get('value'),
                })
                df = df.append(df_new)

    return df


def dfTotal(df):
    total = 0

    for index, row in df.iterrows():
        total += row['value']

    return total


def getAccInfos():
    uid = session['uid']
    json_file = {}
    docRef = store.document('users/' + str(uid))

    dic = store.document('users', session['uid']).get().to_dict()

    for key in dic:
        json_file.update({str(key): str(dic[key])})

    return json_file


def createDataframe(df):
    for i in range(0, 24):
        if i not in df['hour'].values:
            df_new = pd.DataFrame({
                'hour': [i],
                'value': [0],
            })
            df = df.append(df_new)

    df.sort_values(by=['hour'], inplace=True)

    return df
