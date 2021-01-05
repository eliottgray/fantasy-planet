import unittest
from point import Point, CoordinateError


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

        # Cartesian coordinates may be approximate, as they are the result of a transformation.
        self.assertAlmostEqual(expected.x, actual_point.x)
        self.assertAlmostEqual(expected.y, actual_point.y)
        self.assertAlmostEqual(expected.z, actual_point.z)

        # Spherical coordinates must have been stored without being transformed.
        self.assertEqual(expected.alt, actual_point.alt)
        self.assertEqual(expected.lon, actual_point.lon)
        self.assertEqual(expected.alt, actual_point.alt)

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

        # Cartesian coordinates may be approximate, as they are the result of a transformation.
        self.assertAlmostEqual(expected.x, actual_point.x)
        self.assertAlmostEqual(expected.y, actual_point.y)
        self.assertAlmostEqual(expected.z, actual_point.z)

        # Spherical coordinates must have been stored without being transformed.
        self.assertEqual(expected.alt, actual_point.alt)
        self.assertEqual(expected.lon, actual_point.lon)
        self.assertEqual(expected.alt, actual_point.alt)

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
