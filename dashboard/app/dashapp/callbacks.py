from dash.dependencies import Input, Output, ClientsideFunction
import copy
from dash.exceptions import PreventUpdate
from ..python_firebase.firebase_connect import fireauth as fireauth, userdata


def register_callbacks(dashapp):

    # dashapp.clientside_callback(
    #     ClientsideFunction(namespace="clientside", function_name="resize"),
    #     Output("output-clientside", "children"),
    #     [Input("count_graph", "figure")],
    # )

    @dashapp.callback(
        Output("stepsToday", "children"),
        [Input('interval-component', 'n_intervals')])
    def reload_steps_today(n):
        if fireauth.is_user():
            return userdata.daily_steps()
        else:
            raise PreventUpdate


    @dashapp.callback(
        Output("step_goal", "children"),
        [Input('interval-component', 'n_intervals')])
    def reload_steps_goal(n):
        if fireauth.is_user():
            return str(userdata.step_goal()) + '%'
        else:
            raise PreventUpdate


    @dashapp.callback(
        Output("displayName", "children"),
        [Input('interval-component', 'n_intervals')])
    def show_displayName(n):
        if fireauth.is_user():
            return userdata.displayName()
        else:
            raise PreventUpdate


    @dashapp.callback(
        Output("room", "children"),
        [Input('interval-component', 'n_intervals')])
    def show_room(n):
        if fireauth.is_user():
            return userdata.room()
        else:
            raise PreventUpdate

    @dashapp.callback(
        Output("age", "children"),
        [Input('interval-component', 'n_intervals')])
    def get_age(n):
        if fireauth.is_user():
            return userdata.age()
        else:
            raise PreventUpdate


    # @dashapp.callback(
    #     Output("count_graph", "figure"),
    #     [Input('interval-component', 'n_intervals')])
    # def make_count_figure(n):
    #     layout_count = copy.deepcopy(layout)
    #
    #     g = df[["DATE", "STEPS"]]
    #     g = df[(g['DATE'] <= DateToday)]
    #     g.index = g["DATE"]
    #
    #     data = [
    #         dict(
    #             type="bar",
    #             x=g.index,
    #             y=g["STEPS"],
    #             name="Schritte",
    #             showlegend="false",
    #         ),
    #     ]
    #
    #     layout_count["title"] = "Schritte"
    #     layout_count["dragmode"] = "select"
    #     layout_count["showlegend"] = False
    #     layout_count["autosize"] = True
    #
    #     figure = dict(data=data, layout=layout_count)
    #     return figure
    #
    #
    # @dashapp.callback(
    #     Output("stepgoal_graph", "figure"),
    #     [Input('interval-component', 'n_intervals'),
    #      Input('stepsToday', "children")])
    # def make_stepgoal_figure(n, steps):
    #     layout_count = copy.deepcopy(layout)
    #     goal = 10000
    #
    #     data = [
    #         dict(
    #             type="indicator",
    #             mode="number+delta",
    #             value=steps,
    #             domain={'x': [0, 0.5], 'y': [0, 0.5]},
    #             delta={'reference': goal, 'relative': True, 'position': "top"}
    #         ),
    #     ]
    #
    #     layout_count["title"] = "Anderer Graph"
    #     layout_count["dragmode"] = "select"
    #     layout_count["showlegend"] = False
    #     layout_count["autosize"] = True
    #
    #     figure = dict(data=data, layout=layout_count)
    #     return figure