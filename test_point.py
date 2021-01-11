import unittest
from point import Point, CoordinateError


class PointConstructorTest(unittest.TestCase):

    def test_positive_case(self):
        point = Point(lat=30.0, lon=45.0, alt=20.3, x=1.0, y=0.7, z=-1.1)
        self.assertEqual(30.0, point.lat)
        self.assertEqual(45.0, point.lon)
        self.assertEqual(20.3, point.alt)
        self.assertEqual(1.0, point.x)
        self.assertEqual(0.7, point.y)
        self.assertEqual(-1.1, point.z)

    def test_missing_x(self):
        with self.assertRaises(ValueError):
            Point(x=None, y=1, z=0, alt=0.5)

    def test_missing_y(self):
        with self.assertRaises(ValueError):
            Point(x=1, y=None, z=0, alt=0.5)

    def test_missing_z(self):
        with self.assertRaises(ValueError):
            Point(x=1, y=1, z=None, alt=0.5)

    def test_missing_alt(self):
        with self.assertRaises(ValueError):
            Point(x=1, y=1, z=0, alt=None)


class PointFromSphericalCoordinatesTest(unittest.TestCase):

    def run_test(self, lat=0.0, lon=0.0, alt=0.0, x=0.0, y=0.0, z=0.0):
        actual_point = Point.from_spherical(lat=lat, lon=lon, alt=alt)

        # Cartesian coordinates may be approximate, as they are the result of a transformation.
        self.assertAlmostEqual(x, actual_point.x)
        self.assertAlmostEqual(y, actual_point.y)
        self.assertAlmostEqual(z, actual_point.z)

        # Spherical coordinates must have been stored without being transformed.
        self.assertEqual(lat, actual_point.lat)
        self.assertEqual(lon, actual_point.lon)
        self.assertEqual(alt, actual_point.alt)

    def test_x_axis(self):
        """The Origin (0.0, 0.0) and it's Antipode represent the bounds of the X axis."""
        self.run_test(lat=0, lon=0, x=1)
        self.run_test(lat=0, lon=180, x=-1)
        self.run_test(lat=0, lon=-180, x=-1)

    def test_z_axis(self):
        """The poles represent the bounds of the Z axis."""
        self.run_test(lat=90.0, lon=0.0, z=1)
        self.run_test(lat=-90.0, lon=0.0, z=-1)

    def test_y_axis(self):
        """90 and negative 90 represent the bounds of the Y axis."""
        self.run_test(lat=0.0, lon=90, y=1)
        self.run_test(lat=0.0, lon=-90, y=-1)

    def test_all_axes(self):
        self.run_test(lat=45, lon=45, x=0.5, y=0.5, z=0.70710678)

    def test_invalid_latitude(self):
        with self.assertRaises(CoordinateError):
            self.run_test(lat=91)
        with self.assertRaises(CoordinateError):
            self.run_test(lat=-90.0001)

    def test_invalid_longitude(self):
        with self.assertRaises(CoordinateError):
            self.run_test(lon=181)
        with self.assertRaises(CoordinateError):
            self.run_test(lon=-180.0001)


class RotateAroundXAxisTest(unittest.TestCase):

    def setUp(self) -> None:
        self.fixture = Point(x=0.5, y=0.6, z=0.7, alt=1.0)

    def run_test(self, degrees, expected):
        actual_point = self.fixture.rotate_around_x_axis(degrees)
        self.assertEqual(expected, actual_point)

    def test_rotate_90_degrees(self):
        degrees = 90
        expected = Point(x=0.5, y=-0.7, z=0.6, alt=1.0)
        self.run_test(degrees, expected)

    def test_rotate_180_degrees(self):
        degrees = 180
        expected = Point(x=0.5, y=-0.6, z=-0.7, alt=1.0)
        self.run_test(degrees, expected)

    def test_rotate_270_degrees(self):
        degrees = 270
        expected = Point(x=0.5, y=0.7, z=-0.6, alt=1.0)
        self.run_test(degrees, expected)

    def test_rotate_360_degrees(self):
        degrees = 360
        expected = self.fixture.copy()
        self.run_test(degrees, expected)

    def test_rotate_negative_90_degrees(self):
        degrees = -90
        expected = Point(x=0.5, y=0.7, z=-0.6, alt=1.0)
        self.run_test(degrees, expected)

    def test_rotate_45_degrees(self):
        degrees = 45
        expected = Point(x=0.5, y=-0.070710678, z=0.919238815, alt=1.0)
        self.run_test(degrees, expected)


class RotateAroundYAxisTest(unittest.TestCase):

    def setUp(self) -> None:
        self.fixture = Point(x=0.5, y=0.6, z=0.7, alt=1.0)

    def run_test(self, degrees, expected):
        actual_point = self.fixture.rotate_around_y_axis(degrees)
        self.assertEqual(expected, actual_point)

    def test_rotate_90_degrees(self):
        degrees = 90
        expected = Point(x=0.7, y=0.6, z=-0.5, alt=1.0)
        self.run_test(degrees, expected)

    def test_rotate_180_degrees(self):
        degrees = 180
        expected = Point(x=-0.5, y=0.6, z=-0.7, alt=1.0)
        self.run_test(degrees, expected)

    def test_rotate_270_degrees(self):
        degrees = 270
        expected = Point(x=-0.7, y=0.6, z=0.5, alt=1.0)
        self.run_test(degrees, expected)

    def test_rotate_360_degrees(self):
        degrees = 360
        expected = self.fixture.copy()
        self.run_test(degrees, expected)

    def test_rotate_negative_90_degrees(self):
        degrees = -90
        expected = Point(x=-0.7, y=0.6, z=0.5, alt=1.0)
        self.run_test(degrees, expected)

    def test_rotate_45_degrees(self):
        degrees = 45
        expected = Point(x=0.848528137, y=0.6, z=0.141421356, alt=1.0)
        self.run_test(degrees, expected)


class EqualityTest(unittest.TestCase):

    def run_test(self, p1, p2, expected_equality):
        actual_equality = p1 == p2
        self.assertEqual(expected_equality, actual_equality)

    def test_identical_attributes(self):
        x = 1.0
        y = 0.9
        z = 0.8
        alt = 0.7
        lat = 30.0
        lon = 45.0

        p1 = Point(x=x, y=y, z=z, alt=alt, lat=lat, lon=lon)
        p2 = Point(x=x, y=y, z=z, alt=alt, lat=lat, lon=lon)
        self.run_test(p1, p2, expected_equality=True)

    def test_identical_xyzalt_only(self):
        x = 1.0
        y = 0.9
        z = 0.8
        alt = 0.7
        lat1 = 30.0
        lat2 = None
        lon1 = 45.0
        lon2 = None

        p1 = Point(x=x, y=y, z=z, alt=alt, lat=lat1, lon=lon1)
        p2 = Point(x=x, y=y, z=z, alt=alt, lat=lat2, lon=lon2)
        self.run_test(p1, p2, expected_equality=True)

    def test_identical_xyz_different_alt(self):
        x = 1.0
        y = 0.9
        z = 0.8
        alt1 = 0.7
        alt2 = 0.3
        lat = None
        lon = None

        p1 = Point(x=x, y=y, z=z, alt=alt1, lat=lat, lon=lon)
        p2 = Point(x=x, y=y, z=z, alt=alt2, lat=lat, lon=lon)
        self.run_test(p1, p2, expected_equality=False)

    def test_identical_spherical_coordinates(self):
        lat = 30.0
        lon = -90.0
        alt = 0.5

        p1 = Point.from_spherical(lat=lat, lon=lon, alt=alt)
        p2 = Point.from_spherical(lat=lat, lon=lon, alt=alt)
        self.run_test(p1, p2, expected_equality=True)

    def test_identical_lat_lon_different_alt(self):
        lat = 30.0
        lon = -90.0
        alt1 = 0.5
        alt2 = 1.0

        p1 = Point.from_spherical(lat=lat, lon=lon, alt=alt1)
        p2 = Point.from_spherical(lat=lat, lon=lon, alt=alt2)
        self.run_test(p1, p2, expected_equality=False)

    def test_different_spherical_coordinates(self):
        lat1 = 30.0
        lat2 = 45.0
        lon1 = -90.0
        lon2 = -45.0
        alt1 = 0.5
        alt2 = 1.0

        p1 = Point.from_spherical(lat=lat1, lon=lon1, alt=alt1)
        p2 = Point.from_spherical(lat=lat2, lon=lon2, alt=alt2)
        self.run_test(p1, p2, expected_equality=False)


class ReprTest(unittest.TestCase):

    def test_all_values(self):
        x = 1.0
        y = 0.9
        z = 0.8
        alt = 0.7
        lat = 0.6
        lon = 0.5
        point = Point(x=x, y=y, z=z, alt=alt, lat=lat, lon=lon)
        string = repr(point)
        self.assertTrue(str(x) in string)
        self.assertTrue(str(y) in string)
        self.assertTrue(str(z) in string)
        self.assertTrue(str(alt) in string)
        self.assertTrue(str(lat) in string)
        self.assertTrue(str(lon) in string)



