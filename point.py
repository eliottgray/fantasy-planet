import numpy as np
from scipy.spatial import distance
import wgs84

class CoordinateError(Exception):
    pass


def almost_equal(x: float, y: float, epsilon=1e-7):
    """If the difference between two float values is less than epsilon, return True, else return False."""
    if x == y:
        return True
    delta = abs(x - y)
    if delta < epsilon:
        return True
    else:
        return False


class Point:

    def __init__(self, lat: float = None, lon: float = None, alt: float = None, x: float = None, y: float = None, z: float = None):
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
    def from_spherical(lat: float, lon: float, alt: float = 0.0) -> 'Point':
        if not 90 >= lat >= -90:
            raise CoordinateError("Latitude {} encountered, which is not a valid coordinate.".format(str(lat)))
        if not 180 >= lon >= -180:
            raise CoordinateError("Longitude {} encountered, which is not a valid coordinate.".format(str(lat)))
        radLat = np.radians(lat)
        radLon = np.radians(lon)
        cosLat = np.cos(radLat)
        sinLat = np.sin(radLat)
        cosLon = np.cos(radLon)
        sinLon = np.sin(radLon)
        major_squared = wgs84.SEMI_MAJOR_AXIS ** 2
        minor_squared = wgs84.SEMI_MINOR_AXIS ** 2
        prime_vertical_radius_of_curvature = major_squared / np.sqrt(major_squared * (cosLat ** 2) + minor_squared * (sinLat ** 2))

        x = (prime_vertical_radius_of_curvature + alt) * cosLat * cosLon
        y = (prime_vertical_radius_of_curvature + alt) * cosLat * sinLon
        z = ((minor_squared / major_squared) * prime_vertical_radius_of_curvature + alt) * sinLat
        point = Point(lat=lat, lon=lon, alt=alt, x=x, y=y, z=z)
        return point

    def distance(self, other: 'Point') -> float:
        """Euclidean distance to another Point."""
        return distance.euclidean((self.x, self.y, self.z), (other.x, other.y, other.z))

    def midpoint(self, other: 'Point') -> 'Point':
        """Return the midpoint between this point and the given other point."""
        x = (self.x + other.x) / 2
        y = (self.y + other.y) / 2
        z = (self.z + other.z) / 2
        alt = (self.alt + other.alt) / 2
        return Point(x=x, y=y, z=z, alt=alt)

    def rotate_around_x_axis(self, degrees: float) -> 'Point':
        radians = np.radians(degrees)
        sin_rad = np.sin(radians)
        cos_rad = np.cos(radians)
        new_x = self.x
        new_y = self.y * cos_rad - self.z * sin_rad
        new_z = self.z * cos_rad + self.y * sin_rad
        new_alt = self.alt
        return Point(x=new_x, y=new_y, z=new_z, alt=new_alt)

    def rotate_around_y_axis(self, degrees: float) -> 'Point':
        radians = np.radians(degrees)
        sin_rad = np.sin(radians)
        cos_rad = np.cos(radians)
        new_x = self.x * cos_rad + self.z * sin_rad
        new_y = self.y
        new_z = -sin_rad * self.x + self.z * cos_rad
        new_alt = self.alt
        return Point(x=new_x, y=new_y, z=new_z, alt=new_alt)

    def copy(self) -> 'Point':
        return Point(lat=self.lat, lon=self.lon, alt=self.alt, x=self.x, y=self.y, z=self.z)

    def __repr__(self) -> str:
        return "Point[lat: {}, lon: {}, alt: {}, x: {}, y: {}, z: {}]".format(self.lat, self.lon, self.alt, self.x, self.y, self.z)

    def __eq__(self, other: 'Point') -> bool:
        return almost_equal(self.x, other.x) and almost_equal(self.y, other.y) and almost_equal(self.z, other.z) and almost_equal(self.alt, other.alt)

    def __hash__(self):
        return hash((self.x, self.y, self.z, self.alt))

