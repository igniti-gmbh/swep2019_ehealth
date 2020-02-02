import datetime
import functools
import json
import pandas as pd
import plotly.graph_objs as go
from dash.dependencies import Input, Output
from flask import session
from .callback_functions import has_cookie_access
from ..python_firebase.firestore_connect import store
from dateutil import tz


def register_callbacks(dashapp):

    #Verwaltet die Daten für die Schritte
    @dashapp.callback(
        Output('intermediate-value', 'children'),
        [Input('date-picker', 'date'), Input('interval-component', 'n_intervals')], )
    def clean_data(date, n):

        # Verwandelt nur das Datum in Datetime
        date = datetime.datetime.now().strptime(date.split(' ')[0], '%Y-%m-%d')

        # Ueberprueft ob es der aktuelle Tag ist
        if date.date() == datetime.datetime.now().date():
            date = datetime.datetime.now()

        date = normalizeTime(date)
        accountInfos = getAccInfos()

        steps_hours_today = getStepsForDay(date)
        steps_hour_df = createDataframe(steps_hours_today)

        totalSteps = dfTotal(steps_hour_df)

        datasets = {
            'stepsHoursDf': steps_hour_df.to_json(orient='split', date_format='iso'),
            'totalSteps': totalSteps,
            'accountInfos': accountInfos,
        }

        return json.dumps(datasets)

    # Verwaltet die Daten für die Schritte und sich nicht so oft ändernde
    @dashapp.callback(
        Output('intermediate-value-room', 'children'),
        [Input('interval-component', 'n_intervals')], )
    def clean_data(n):
        accountInfos = getAccInfos()
        room = accountInfos['room']

        arduinoData = getCurrentArduino(room)


        temperature_hours_today = getTemperatureForDay(room)
        temperature_hour_df = createDataframe(temperature_hours_today, None)

        gas_hours_today = getGasForDay(room)
        gas_hour_df = createDataframe(gas_hours_today,None)

        datasets = {
            'temperatureHoursDf': temperature_hour_df.to_json(orient='split', date_format='iso'),
            'gasHoursDf': gas_hour_df.to_json(orient='split', date_format='iso'),
            'arduinoData': arduinoData,
            'accountInfos': accountInfos,
        }

        return json.dumps(datasets)

    #Anzeige von Uhrzeit und Verbindung
    @dashapp.callback(
        Output("connection_status", "children"),
        [Input('intermediate-value', 'children')])
    def show_status(n):
        if has_cookie_access():
            return "Verbunden"
        else:
            return "Nicht Verbunden"

    @dashapp.callback(
        Output("current_date", "children"),
        [Input('intermediate-value', 'children')])
    def show_date(json_data):
        timestamp = getCurrentTime()
        return timestamp.date().strftime('%d.%m.%Y')

    @dashapp.callback(
        Output("current_time", "children"),
        [Input('intermediate-value', 'children')])
    def show_time(json_data):
        timestamp = getCurrentTime()
        return timestamp.time().strftime('%H:%M')

#Anzeige der Daten in der linken Leiste

    @dashapp.callback(
        Output("displayName", "children"),
        [Input('intermediate-value-room', 'children')])
    def show_display_name(json_data):
        data = json.loads(json_data)
        name = data['accountInfos']['name']

        if name is None:
            return '/'
        else:
            return name

    @dashapp.callback(
        Output("position", "children"),
        [Input('intermediate-value-room', 'children')])
    def show_position(json_data):
        data = json.loads(json_data)
        position = data['accountInfos']['position']

        if position is None:
            return '/'
        else:
            return position

    @dashapp.callback(
        Output("room", "children"),
        [Input('intermediate-value-room', 'children')])
    def show_room(json_data):
        data = json.loads(json_data)
        room = data['accountInfos']['room']

        if room is None:
            return '/'
        else:
            return str(room)

    @dashapp.callback(
        Output("age", "children"),
        [Input('intermediate-value-room', 'children')])
    def show_age(json_data):
        data = json.loads(json_data)
        age = data['accountInfos']['age']

        if age is None:
            return '/'
        else:
            return str(age)

    #Schrittziel und Schritte allgemein

    @dashapp.callback(
        Output("step_goal", "children"),
        [Input('intermediate-value', 'children')])
    @functools.lru_cache(maxsize=32)
    def reload_steps_goal(json_data):
        data = json.loads(json_data)
        total_steps = data['totalSteps']
        step_goal = data['accountInfos']['stepgoal']

        goal_reached = total_steps / int(step_goal) * 100
        goal_reached = round(goal_reached, 2)

        if goal_reached > 100:
            goal_reached = 100

        return str(goal_reached) + '%'

    @dashapp.callback(
        Output("stepsToday", "children"),
        [Input('intermediate-value', 'children')])
    def reload_steps_today(json_data):
        data = json.loads(json_data)

        return data['totalSteps']

    # Zeigt die Schritte nach Stunden an

    @dashapp.callback(
        Output('hours_graph', "figure"),
        [Input('intermediate-value', 'children')])
    def show_graph(json_data):

        data = json.loads(json_data)
        df = pd.read_json(data['stepsHoursDf'], orient='split')

        layout = dict(autosize=True,
                      margin=dict(l=30, r=30, b=20, t=40),
                      hovermode="closest",
                      plot_bgcolor="#262a30",
                      paper_bgcolor="#262a30",
                      legend=dict(font=dict(size=10), orientation="h"),
                      xaxis=dict(type='category', title='Stunde', color='#ededed'),
                      yaxis=dict(title='Schritte', rangemode='nonnegative', color='#ededed'), )

        data = [go.Bar(
            x=df['hour'],
            y=df['value'],
        )]

        fig = go.Figure(data=data, layout=layout)
        return fig


    #Anzeigen der Arduino Daten

    @dashapp.callback(
        Output("airquality", "children"),
        [Input('intermediate-value-room', 'children')])
    def reload_air_quality(json_data):
        data = json.loads(json_data)

        value = data['arduinoData']['gas']

        if value is None:
            return '/'

        if value <= 0.2:
            return "Sehr gut"
        elif value <= 0.4:
            return "Gut"
        elif value <= 0.6:
            return "Okay"
        elif value <= 0.8:
            return "Schlecht"
        else:
            return "Sehr Schlecht"

    @dashapp.callback(
        Output("temperature", "children"),
        [Input('intermediate-value-room', 'children')])
    def reload_temperature(json_data):
        data = json.loads(json_data)
        value = data['arduinoData']['temperature']

        if value is None:
            return '/'

        return str(value) + ' °C'

    @dashapp.callback(
        Output("humidity", "children"),
        [Input('intermediate-value-room', 'children')])
    def reload_air_quality(json_data):
        data = json.loads(json_data)
        value = data['arduinoData']['humidity']

        if value is None:
            return '/'

        return str(value) + '%'

    @dashapp.callback(
        Output("pressure", "children"),
        [Input('intermediate-value-room', 'children')])
    def reload_air_quality(json_data):
        data = json.loads(json_data)
        value = data['arduinoData']['pressure']

        if value is None:
            return '/'

        return str(value) + ' hPa'


    @dashapp.callback(
        Output('temperature_graph', "figure"),
        [Input('intermediate-value-room', 'children')])
    def show_graph(json_data):

        data = json.loads(json_data)
        df = pd.read_json(data['temperatureHoursDf'], orient='split')

        layout = dict(autosize=True,
                      margin=dict(l=30, r=30, b=20, t=40),
                      hovermode="closest",
                      plot_bgcolor="#262a30",
                      paper_bgcolor="#262a30",
                      legend=dict(font=dict(size=10), orientation="h"),
                      xaxis=dict(type='category', title='Stunde', color='#ededed'),
                      yaxis=dict(title='Luftqualität', rangemode='nonnegative', color='#ededed'), )

        data = [go.Scatter(
            x=df['hour'],
            y=df['value'],
            connectgaps=True,
        )]

        fig = go.Figure(data=data, layout=layout)
        return fig


    @dashapp.callback(
        Output('airquality_graph', "figure"),
        [Input('intermediate-value-room', 'children')])
    @functools.lru_cache(maxsize=32)
    def show_graph(json_data):

        data = json.loads(json_data)
        df = pd.read_json(data['gasHoursDf'], orient='split')

        layout = dict(autosize=True,
                      margin=dict(l=30, r=30, b=20, t=40),
                      hovermode="closest",
                      plot_bgcolor="#262a30",
                      paper_bgcolor="#262a30",
                      legend=dict(font=dict(size=10), orientation="h"),
                      xaxis=dict(type='category', title='Stunde', color='#ededed'),
                      yaxis=dict(title='Temperatur', rangemode='nonnegative', color='#ededed'), )

        data = [go.Scatter(
            x=df['hour'],
            y=df['value'],
            connectgaps=True,
        )]

        fig = go.Figure(data=data, layout=layout)
        return fig


    # Zeigt Schritte nach Auswahl an
    @dashapp.callback(
        Output("days_graph", "figure"),
        [Input('date-picker-range', 'start_date'), Input('date-picker-range', 'end_date')])
    @functools.lru_cache(maxsize=32)
    def show_day_graph(start_date, end_date):

        layout = dict(autosize=True,
                      margin=dict(l=30, r=30, b=20, t=40),
                      hovermode="closest",
                      plot_bgcolor="#262a30",
                      paper_bgcolor="#262a30",
                      legend=dict(font=dict(size=10), orientation="h"),
                      xaxis=dict(type='category', title='Tage', color='#ededed'),
                      yaxis=dict(title='steps', rangemode='nonnegative', color='#ededed'),
                      )

        if end_date is None:
            fig = go.Figure(layout=layout)
            return fig

        start_date = datetime.datetime.strptime(start_date.split(' ')[0], '%Y-%m-%d')
        end_date = datetime.datetime.strptime(end_date.split(' ')[0], '%Y-%m-%d')

        dates = []
        values = []

        timedelta = end_date - start_date

        date_list = [start_date + datetime.timedelta(days=x) for x in range(timedelta.days + 1)]

        for date in date_list:
            df_values = getStepsForDay(date)
            total = dfTotal(df_values)
            dates.append(date.strftime('%d.%m.%Y'))
            values.append(total)

        data = [go.Bar(
            x=dates,
            y=values,
        )]

        fig = go.Figure(data=data, layout=layout)
        return fig


# Holt Schrittdaten des Tages
@functools.lru_cache(maxsize=32)
def getStepsForDay(date):
    range_start = 23
    range_end = -1
    range_iteration = -1

    date = date.replace(hour=23, minute=59, second=59)

    # dataframe aus dem graph erstellt wird
    df = pd.DataFrame(columns=['hour', 'value'])

    colRef = store.collection(
        'users/' + str(session['uid']) + '/' + str(date.year) + '/' + str(date.month) + '/'
        + str(date.day))
    snapshot = colRef.list_documents()

    for doc in snapshot:
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


@functools.lru_cache(maxsize=6)
def getTemperatureForDay(room):

    date = getCurrentTime()

    range_start = 23
    range_end = -1
    range_iteration = -1

    date = date.replace(hour=23, minute=59, second=59)

    # dataframe aus dem graph erstellt wird
    df = pd.DataFrame(columns=['hour', 'value'])

    colRef = store.collection(
        'rooms/' + str(room) + '/' + str(date.year) + '/' + str(date.month) + '/'
        + str(date.day))
    snapshot = colRef.list_documents()

    for doc in snapshot:
        docID = doc.id

        for i in range(range_start, range_end, range_iteration):

            iterated_hour = date - datetime.timedelta(hours=i)

            if int(docID) == iterated_hour.hour:
                df_new = pd.DataFrame({
                    'hour': [iterated_hour.hour],
                    'value': doc.get().get('temperatureAverage'),
                })
                df = df.append(df_new)

    return df


@functools.lru_cache(maxsize=6)
def getGasForDay(room):

    date = getCurrentTime()


    range_start = 23
    range_end = -1
    range_iteration = -1

    date = date.replace(hour=23, minute=59, second=59)

    # dataframe aus dem graph erstellt wird
    df = pd.DataFrame(columns=['hour', 'value'])

    colRef = store.collection(
        'rooms/' + str(room) + '/' + str(date.year) + '/' + str(date.month) + '/'
        + str(date.day))
    snapshot = colRef.list_documents()

    for doc in snapshot:
        docID = doc.id

        for i in range(range_start, range_end, range_iteration):

            iterated_hour = date - datetime.timedelta(hours=i)

            if int(docID) == iterated_hour.hour:
                df_new = pd.DataFrame({
                    'hour': [iterated_hour.hour],
                    'value': doc.get().get('gasAverage'),
                })
                df = df.append(df_new)

    return df


def dfTotal(df):
    total = 0

    for index, row in df.iterrows():
        total += row['value']

    return total


@functools.lru_cache(maxsize=32)
def getAccInfos():
    json_file = {}

    dic = store.document('users', session['uid']).get().to_dict()

    for key in dic:
        json_file.update({str(key): str(dic[key])})

    return json_file


def createDataframe(df, empty_values=0):
    for i in range(0, 24):
        if i not in df['hour'].values:
            df_new = pd.DataFrame({
                'hour': [i],
                'value': empty_values,
            })
            df = df.append(df_new)

    df.sort_values(by=['hour'], inplace=True)

    return df


def getCurrentArduino(room):

    date = getCurrentTime()

    docRef = store.document(
        'rooms/' + str(room) + '/' + str(date.year) + '/' + str(date.month) + '/'
        + str(date.day) + '/' + str(date.hour))

    while True:

        if docRef.get().exists is False:

            date = date - datetime.timedelta(hours=1)

            docRef = store.document(
                'rooms/' + str(room) + '/' + str(date.year) + '/' + str(date.month) + '/'
                + str(date.day) + '/' + str(date.hour))

            if docRef.get().exists is False:
                valueJSON = {
                    'temperature': None,
                    'gas': None,
                    'humidity': None,
                    'pressure': None,
                }

                return valueJSON
        break

    docDic = docRef.get().to_dict()

    valueJSON = {
        'temperature': docDic['temperatureCurrent'],
        'gas': docDic['gasCurrent'],
        'humidity': docDic['humidityCurrent'],
        'pressure': docDic['pressureCurrent'],
    }

    for key in valueJSON:
        if valueJSON[key] is None:
            valueJSON[key] = 0

    return valueJSON


def getCurrentTime():
    return normalizeTime(datetime.datetime.now())


def normalizeTime(utc):
    from_zone = tz.tzlocal()
    to_zone = tz.gettz('Europe/Berlin')

    utc = utc.replace(tzinfo=from_zone)

    # Convert time zone
    central = utc.astimezone(to_zone)

    return central


def datetimeJSONconvert(o):
    if isinstance(o, datetime.datetime):
        return o.__str__()
