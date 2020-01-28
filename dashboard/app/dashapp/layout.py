import datetime

import dash_core_components as dcc
import dash_html_components as html
import pandas as pd

# Time and date
DateToday = pd.Timestamp.now().date()
TimeNow = pd.Timestamp.now().time()

colors = {
    'background': '#262a30',
    'text': '#ededed'
}

loggedin = html.Div(
    [
        html.Div(
            [
                html.Div(
                    [
                        html.P(id="current_date"),
                        html.P(id="current_time"),
                    ],
                    className="one-third column",
                ),
                html.Div(
                    [
                        html.Div(
                            [
                                html.H3(
                                    "Ignite E-Health Initiative",
                                    style={"margin-bottom": "0px"},
                                ),
                                html.H5(
                                    id="connection_status", style={"margin-top": "0px"}
                                ),
                            ]
                        )
                    ],
                    className="one-half column",
                    id="title",
                ),
                html.Div(
                    [
                        html.A(
                            html.Button("Back", id="back-button"),
                            href="/profile",
                        )
                    ],
                    className="one-third column",
                    id="button",
                ),
            ],
            id="header",
            className="row flex-display",
            style={"margin-bottom": "25px"},
        ),
        html.Div(
            [
                html.Div(
                    [
                        html.P(
                            "Name",
                            className="control_label",
                        ),
                        html.P(
                            id="displayName",
                            className="control_info",
                        ), html.P(
                        "Position",
                        className="control_label",
                    ), html.P(
                        id="position",
                        className="control_info",
                    ), html.P(
                        "Alter",
                        className="control_label",
                    ), html.P(
                        id="age",
                        className="control_info",
                    ), html.P(
                        "Raum",
                        className="control_label",
                    ), html.P(
                        id='room',
                        className="control_info",
                    ), html.P("Datum", className="control_label"),
                        dcc.DatePickerSingle(
                            id='date-picker',
                            date=str(datetime.datetime.now()),
                            display_format='DD.MM.YYYY',
                        ), html.Div(id='spacer'),
                    ],
                    className="pretty_container four columns",
                    id="cross-filter-options",

                ),
                html.Div(
                    [
                        html.Div(
                            [
                                html.Div(
                                    [html.H6(id="stepsToday"), html.P("Schritte")],
                                    className="mini_container",
                                ),
                                html.Div(
                                    [html.H6(id="step_goal"), html.P("Schrittziel")],
                                    className="mini_container",
                                ),
                            ],
                            id="info-container-one",
                            className="row container-display",
                        ),
                        html.Div(
                            [dcc.Graph(id="hours_graph",
                                       config={'displayModeBar': False},
                                       figure={
                                           'layout': {
                                               'plot_bgcolor': colors['background'],
                                               'paper_bgcolor': colors['background'],
                                               'font': {
                                                   'color': colors['text']
                                               }
                                           }
                                       }
                                       )],
                            id='countGraphContainer',
                            className="pretty_container",

                        ),
                    ],
                    id="right-column",
                    className="nine columns",
                ),
            ],
            className="row flex-display",
        ),
        html.Div([
            html.Div(
                [
                    html.Div(
                        [html.H6(id="airquality"), html.P("Luftqualität")],

                        className="mini_container",
                    ),
                    html.Div(
                        [html.H6(id="temperature"), html.P("Temperatur")],
                        className="mini_container",
                    ),
                    html.Div(
                        [html.H6(id="humidity"), html.P("Luftfeuchtigkeit")],
                        className="mini_container",
                    ),
                    html.Div(
                        [html.H6(id="pressure"), html.P("Luftdruck")],
                        className="mini_container",
                    ),
                ],
                className="row container-display",
                id="info-container-two",
            ),

        ],
            className="twelve columns",
        ),
        # html.Div(
        #     [
        #         html.Div(
        #             [dcc.Graph(id="gas_graph",
        #                        config={'displayModeBar': False},
        #                        figure={
        #                            'layout': {
        #                                'plot_bgcolor': colors['background'],
        #                                'paper_bgcolor': colors['background'],
        #                                'font': {
        #                                    'color': colors['text']
        #                                }
        #                            }
        #                        }
        #                        )],
        #             id='gasHistory',
        #             className="pretty_container six columns",
        #
        #         ),
        #         html.Div(
        #             [dcc.Graph(id="temperature_graph",
        #                        config={'displayModeBar': False},
        #                        figure={
        #                            'layout': {
        #                                'plot_bgcolor': colors['background'],
        #                                'paper_bgcolor': colors['background'],
        #                                'font': {
        #                                    'color': colors['text']
        #                                }
        #                            }
        #                        }
        #                        )],
        #             id='temperatureHistory',
        #             className="pretty_container six columns",
        #
        #         ), html.Div(
        #         [dcc.Graph(id="humidity_graph",
        #                    config={'displayModeBar': False},
        #                    figure={
        #                        'layout': {
        #                            'plot_bgcolor': colors['background'],
        #                            'paper_bgcolor': colors['background'],
        #                            'font': {
        #                                'color': colors['text']
        #                            }
        #                        }
        #                    }
        #                    )],
        #         id='humidityHistory',
        #         className="pretty_container six columns",
        #
        #     ),html.Div(
        #         [dcc.Graph(id="pressure_graph",
        #                    config={'displayModeBar': False},
        #                    figure={
        #                        'layout': {
        #                            'plot_bgcolor': colors['background'],
        #                            'paper_bgcolor': colors['background'],
        #                            'font': {
        #                                'color': colors['text']
        #                            }
        #                        }
        #                    }
        #                    )],
        #         id='pressureHistory',
        #         className="pretty_container six columns",
        #
        #     ),
        #     ],
        #     className="row flex-display",
        # ),
        html.Div([
            html.Div(
                [dcc.DatePickerRange(
                    id='date-picker-range',
                    start_date=str(datetime.datetime.now() - datetime.timedelta(days=7)),
                    end_date_placeholder_text='Select a date!',
                    display_format='DD.MM.YYYY'
                ), dcc.Graph(id="days_graph",
                             config={'displayModeBar': False},
                             figure={
                                 'layout': {
                                     'plot_bgcolor': colors['background'],
                                     'paper_bgcolor': colors['background'],
                                     'font': {
                                         'color': colors['text']
                                     }
                                 }
                             }
                             )],
                className="pretty_container twelve colums",
            ),
        ],
            className="row flex-container",
        ),
        # Hidden div inside the app that stores the intermediate value
        html.Div(
            id='intermediate-value',
            style={'display': 'none'}
        ),

        dcc.Interval(
            id='interval-component',
            interval=1 * 300000,  # in milliseconds
            n_intervals=0
        ),

    ],
    id="mainContainer",
    style={"display": "flex", "flex-direction": "column"},
)

loggedout = html.Div(
    [
        html.Div(
            [
                html.Div(
                    [
                        html.P(DateToday.strftime('%d.%m.%Y')),
                        html.P(TimeNow.strftime('%H:%M')),
                    ],
                    className="one-third column",
                ),
                html.Div(
                    [
                        html.Div(
                            [
                                html.H3(
                                    "Du bist nicht eingeloggt",
                                    style={"margin-bottom": "0px"},
                                ),
                                html.H5(
                                    id="connection_status", style={"margin-top": "0px"}
                                ),
                            ]
                        )
                    ],
                    className="one-half column",
                    id="title",
                ),
                html.Div(
                    [
                        html.A(
                            html.Button("Logout", id="logout-button"),
                            href="#",
                        )
                    ],
                    className="one-third column",
                    id="button",
                ),
            ],
            id="header",
            className="row flex-display",
            style={"margin-bottom": "25px"},
        ),
        dcc.Interval(
            id='interval-component',
            interval=1 * 10000,  # in milliseconds
            n_intervals=0
        ),

    ],
    id="mainContainer",
    style={"display": "flex", "flex-direction": "column"},
)


def serve_layout():
    return loggedin


# HTML Header
index_string = '''
<!DOCTYPE html>
<html>
    <head>
        {%metas%}
        <title>Igniti Health</title>
        {%favicon%}
        {%css%}
    </head>
    <body>
        {%app_entry%}
        <footer>
            {%config%}
            {%scripts%}
            {%renderer%}
        </footer>
    </body>
</html>
'''
