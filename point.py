import numpy as np
from math import radians


class Point:

    def __init__(self, lat=None, lon=None, x=None, y=None, z=None):
        self._lat = lat
        self._lon = lon
        self._x = x
        self._y = y
        self._z = z


    @staticmethod
    def from_spherical(lat: float, lon: float):
        # TODO: Avoid out-of-bounds coordinates.
        cosLat = np.cos(radians(lat))
        sinLat = np.sin(radians(lat))
        C = 1 / np.sqrt(cosLat ** 2 + sinLat ** 2)
        x = C * cosLat * np.cos(radians(lon))
        y = C * cosLat * np.sin(radians(lon))
        z = C * sinLat
        point = Point(lat=lat, lon=lon, x=x, y=y, z=z)
        return point

    def get_lon(self):
        return self._lon

    def get_lat(self):
        return self._lat

    def get_x(self):
        return self._x

    def get_y(self):
        return self._y

    def get_z(self):
        return self._z

    def __repr__(self):
        return "lat: {}, lon: {}, x: {}, y: {}, z: {}".format(self._lat, self._lon, self._x, self._y, self._z)
