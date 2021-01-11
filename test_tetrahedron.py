import unittest
from tetrahedron import Tetrahedron
from point import Point


class TetrahedronTest(unittest.TestCase):

    def test_positive_case(self):
        a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=0.0, alt=1.0)
        c = Point(x=-1.0, y=1.0, z=0.0, alt=1.0)
        d = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        self.assertEqual(a, tetra.a)
        self.assertEqual(b, tetra.b)
        self.assertEqual(c, tetra.c)
        self.assertEqual(d, tetra.d)


class RotateTetrahedronTest(unittest.TestCase):

    def compare_points(self, expected_point, actual_point):

        # Cartesian coordinates may be approximate, as they are the result of a transformation.
        self.assertAlmostEqual(expected_point.x, actual_point.x)
        self.assertAlmostEqual(expected_point.y, actual_point.y)
        self.assertAlmostEqual(expected_point.z, actual_point.z)

        # Spherical coordinates must have been stored without being transformed.
        self.assertEqual(expected_point.lat, actual_point.lat)
        self.assertEqual(expected_point.lon, actual_point.lon)
        self.assertEqual(expected_point.alt, actual_point.alt)

    def compare_tetrahedrons(self, expected_tetrahedron, actual_tetrahedron):
        self.compare_points(expected_tetrahedron.a, actual_tetrahedron.a)
        self.compare_points(expected_tetrahedron.b, actual_tetrahedron.b)
        self.compare_points(expected_tetrahedron.c, actual_tetrahedron.c)
        self.compare_points(expected_tetrahedron.d, actual_tetrahedron.d)

    def setUp(self) -> None:
        """Create a fixture that is quite obviously """
        a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)   # Top of pyramid.
        b = Point(x=1.0, y=0.0, z=-1.0, alt=0.9)
        c = Point(x=-1.0, y=1.0, z=-1.0, alt=0.8)
        d = Point(x=-1.0, y=-1.0, z=-1.0, alt=0.7)
        self.tetra = Tetrahedron(a=a, b=b, c=c, d=d)

    def test_rotate_around_x_axis_90_degrees(self):
        a = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=0.0, alt=0.9)
        c = Point(x=-1.0, y=1.0, z=1.0, alt=0.8)
        d = Point(x=-1.0, y=1.0, z=-1.0, alt=0.7)
        expected = Tetrahedron(a=a, b=b, c=c, d=d)
        actual = self.tetra.rotate_around_x_axis(90)
        self.compare_tetrahedrons(expected_tetrahedron=expected, actual_tetrahedron=actual)


class ContainsPointTest(unittest.TestCase):

    def setUp(self) -> None:
        a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=0.0, alt=1.0)
        c = Point(x=-1.0, y=1.0, z=0.0, alt=1.0)
        d = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        self.tetra = Tetrahedron(a=a, b=b, c=c, d=d)

    def test_vertex(self):
        """Point identical to a vertex of the tetrahedron."""
        vertex = self.tetra.a.copy()
        self.assertTrue(self.tetra.contains(vertex))

    def test_positive_case(self):
        """Point far inside the bounds of the tetrahedron."""
        point = Point(x=0.0, y=0.0, z=0.5, alt=0.0)
        self.assertTrue(self.tetra.contains(point))

    def test_negative_case(self):
        """Point far outside the bounds of the tetrahedron."""
        point = Point(x=0.0, y=0.0, z=-1.0, alt=0.0)
        self.assertFalse(self.tetra.contains(point))

    def test_on_edge(self):
        """By setting the fixture tetrahedron to include a right triangle, a point on the edge can be easily tested."""
        point = Point(x=0.0, y=-0.5, z=0.5, alt=0.0)
        self.assertTrue(self.tetra.contains(point))


class SideLengthTest(unittest.TestCase):

    def run_test(self, expected, tetra):
        actual = tetra.longest_side_len()
        self.assertAlmostEqual(expected, actual)

    def test_simple_case(self):
        """Side a-d longer than the others."""
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        expected = 2.8284271247461903
        self.run_test(expected, tetra)

    def test_rotated_case(self):
        """Side a-d is longer than the others, and the shape is rotated before calculating the length."""
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        tetra = tetra.rotate_around_x_axis(29)
        tetra = tetra.rotate_around_y_axis(123)
        expected = 2.8284271247461903
        self.run_test(expected, tetra)

    def test_equal_sides(self):
        a = Point(x=1.0, y=1.0, z=1.0, alt=1.0)
        b = Point(x=-1.0, y=-1.0, z=1.0, alt=1.0)
        c = Point(x=-1.0, y=1.0, z=-1.0, alt=1.0)
        d = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        expected = 2.8284271247461903
        self.run_test(expected, tetra)

