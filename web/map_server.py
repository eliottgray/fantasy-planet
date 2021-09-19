from flask import Flask, send_file, render_template

app = Flask(__name__)


# @app.route("/")
# def hello_world():
#     return "<p>Hello, World!</p>"

@app.route("/")
def hello():
    return render_template('index.html') #"Hello World!"


@app.route("/tiles/<int:z>/<int:x>/<int:y>.png", methods=['GET', 'POST'])
def get_files(z, x, y):
    # https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    """
    Send a map tile at zoom level z, column x, row y.
    """
    path = f'tiles\\{z}\\{x}\\{y}.png'

    return send_file(
        path,
        as_attachment=False,
        mimetype='image/png'
    )


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, threaded=True, debug=True)
