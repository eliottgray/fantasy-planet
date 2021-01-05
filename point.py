import numpy as np


class CoordinateError(Exception):
    pass


class Point:

    def __init__(self, lat=None, lon=None, alt=None, x=None, y=None, z=None):
        self.lat = lat
        self.lon = lon
        self.alt = alt
        self.x = x
        self.y = y
        self.z = z
        if alt is None:
            raise ValueError("Altitude value is None.")
        if x is None:
            raise ValueError("X value is None.")
        if y is None:
            raise ValueError("Y value is None.")
        if z is None:
            raise ValueError("Z value is None.")

    @staticmethod
    def from_spherical(lat: float, lon: float, alt: float = 0.0):
        if not 90 >= lat >= -90:
            raise CoordinateError("Latitude {} encountered, which is not a valid coordinate.".format(str(lat)))
        if not 180 >= lon >= -180:
            raise CoordinateError("Longitude {} encountered, which is not a valid coordinate.".format(str(lat)))
        cosLat = np.cos(np.radians(lat))
        sinLat = np.sin(np.radians(lat))
        C = 1 / np.sqrt(cosLat ** 2 + sinLat ** 2)
        x = (C + alt) * cosLat * np.cos(np.radians(lon))
        y = (C + alt) * cosLat * np.sin(np.radians(lon))
        z = (C + alt) * sinLat
        point = Point(lat=lat, lon=lon, alt=alt, x=x, y=y, z=z)
        return point

    def rotate_around_x_axis(self, degrees):
        radians = np.radians(degrees)
        sin_rad = np.sin(radians)
        cos_rad = np.cos(radians)
        new_x = self.x
        new_y = self.y * cos_rad - self.z * sin_rad
        new_z = self.z * cos_rad + self.y * sin_rad
        new_alt = self.alt
        return Point(x=new_x, y=new_y, z=new_z, alt=new_alt)

    def rotate_around_y_axis(self, degrees):
        radians = np.radians(degrees)
        sin_rad = np.sin(radians)
        cos_rad = np.cos(radians)
        new_x = self.x * cos_rad + self.z * sin_rad
        new_y = self.y
        new_z = -sin_rad * self.x + self.z * cos_rad
        new_alt = self.alt
        return Point(x=new_x, y=new_y, z=new_z, alt=new_alt)

    def copy(self):
        return Point(lat=self.lat, lon=self.lon, alt=self.alt, x=self.x, y=self.y, z=self.z)

    def __repr__(self):
        return "Point[lat: {}, lon: {}, alt: {}, x: {}, y: {}, z: {}]".format(self.lat, self.lon, self.alt, self.x, self.y, self.z)
