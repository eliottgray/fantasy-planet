import numpy as np


class CoordinateError(Exception):
    pass


class Point:

    def __init__(self, lat=None, lon=None, alt=None, x=None, y=None, z=None):
        if not 90 >= lat >= -90:
            raise CoordinateError("Latitude {} encountered, which is not a valid coordinate.".format(str(lat)))
        if not 180 >= lon >= -180:
            raise CoordinateError("Longitude {} encountered, which is not a valid coordinate.".format(str(lat)))
        self._lat = lat
        self._lon = lon
        self._alt = alt
        self._x = x
        self._y = y
        self._z = z

    @staticmethod
    def from_spherical(lat: float, lon: float, alt: float = 0.0):
        cosLat = np.cos(np.radians(lat))
        sinLat = np.sin(np.radians(lat))
        C = 1 / np.sqrt(cosLat ** 2 + sinLat ** 2)
        x = (C + alt) * cosLat * np.cos(np.radians(lon))
        y = (C + alt) * cosLat * np.sin(np.radians(lon))
        z = (C + alt) * sinLat
        point = Point(lat=lat, lon=lon, alt=alt, x=x, y=y, z=z)
        return point

    def get_lon(self):
        return self._lon

    def get_lat(self):
        return self._lat

    def get_alt(self):
        return self._alt

    def get_x(self):
        return self._x

    def get_y(self):
        return self._y

    def get_z(self):
        return self._z

    def __repr__(self):
        return "Point[lat: {}, lon: {}, alt: {}, x: {}, y: {}, z: {}]".format(self._lat, self._lon, self._alt, self._x, self._y, self._z)
