import dash_core_components as dcc
import dash_html_components as html
import pandas as pd

# Time and date
DateToday = pd.Timestamp.now().date()
TimeNow = pd.Timestamp.now().time()

layout = html.Div(
    [
        dcc.Store(id="aggregate_data"),
        # empty Div to trigger javascript file for graph resizing
        html.Div(id="output-clientside"),
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
                                    "Ignite E-Health Initiative",
                                    style={"margin-bottom": "0px"},
                                ),
                                html.H5(
                                    "Version 0.1", style={"margin-top": "0px"}
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
                    ),
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
                                html.Div(
                                    [html.H6("/"), html.P("Schrittrangliste")],
                                    className="mini_container",
                                ),
                                html.Div(
                                    [html.H6("/"), html.P("Temparatur")],
                                    className="mini_container",
                                ),
                            ],
                            id="info-container-one",
                            className="row container-display",
                        ),
                        html.Div(
                            [
                                html.Div(
                                    [html.H6("/"), html.P("Luftfeuchtigkeit")],
                                    className="mini_container",
                                ),
                                html.Div(
                                    [html.H6("/"), html.P("Letzte Bewegung")],
                                    className="mini_container",
                                ),
                                html.Div(
                                    [html.H6("/"), html.P("bis zur nächsten Bewegung")],
                                    className="mini_container",
                                ),
                            ],
                            id="info-container-two",
                            className="row container-display",
                        ),
                    ],
                    id="right-column",
                    className="nine columns",
                ),
            ],
            className="row flex-display",
        ),
        html.Div(
            [
                html.Div(
                    [dcc.Graph(id="stepgoal_graph")],
                    className="pretty_container four columns",
                ),
                html.Div(
                    [dcc.Graph(id="count_graph",
                               config={
                                   'displayModeBar': False
                               })],
                    className="pretty_container",
                    id="countGraphContainer"
                ),
            ],
            className="row flex-display",
        ),

        dcc.Interval(
            id='interval-component',
            interval=1 * 100000,  # in milliseconds
            n_intervals=0
        ),

    ],
    id="mainContainer",
    style={"display": "flex", "flex-direction": "column"},
)

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
