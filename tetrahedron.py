from point import Point
from scipy.spatial import Delaunay
from scipy.optimize import linprog
import numpy as np
import typing


DEFAULT_ALTITUDE = -.02   # Just below 'sea level' of 0 altitude.


class Tetrahedron:

    def __init__(self, a: Point, b: Point, c: Point, d: Point):
        self.a = a
        self.b = b
        self.c = c
        self.d = d
        # TODO: Profile space/time difference of pre-generating a tuple for each point, or just defining them here.
        # Stack overflow suggestion to use scipy Delaunay class: https://stackoverflow.com/a/16898636
        self._longest_side_len = None

    @staticmethod
    def build_default(alt=25000000) -> 'Tetrahedron':
        """
        Creates a Tetrahedron with default orientation and altitudes.
        :return: Default Tetrahedron.
        """
        a = Point.from_spherical(lat=90, lon=0, alt=alt)
        b = Point.from_spherical(lat=-30, lon=0, alt=alt)
        c = Point.from_spherical(lat=-30, lon=120, alt=alt)
        d = Point.from_spherical(lat=-30, lon=-120, alt=alt)
        a.alt = DEFAULT_ALTITUDE
        b.alt = DEFAULT_ALTITUDE
        c.alt = DEFAULT_ALTITUDE
        d.alt = DEFAULT_ALTITUDE
        default = Tetrahedron(a=a, b=b, c=c, d=d)
        return default

    def rotate_around_x_axis(self, degrees: float) -> 'Tetrahedron':
        new_a = self.a.rotate_around_x_axis(degrees)
        new_b = self.b.rotate_around_x_axis(degrees)
        new_c = self.c.rotate_around_x_axis(degrees)
        new_d = self.d.rotate_around_x_axis(degrees)
        return Tetrahedron(a=new_a, b=new_b, c=new_c, d=new_d)

    def rotate_around_y_axis(self, degrees: float) -> 'Tetrahedron':
        new_a = self.a.rotate_around_y_axis(degrees)
        new_b = self.b.rotate_around_y_axis(degrees)
        new_c = self.c.rotate_around_y_axis(degrees)
        new_d = self.d.rotate_around_y_axis(degrees)
        return Tetrahedron(a=new_a, b=new_b, c=new_c, d=new_d)

    def contains_old(self, point: Point) -> bool:
        hull = Delaunay(np.array([(self.a.x, self.a.y, self.a.z), (self.b.x, self.b.y, self.b.z), (self.c.x, self.c.y, self.c.z), (self.d.x, self.d.y, self.d.z)]))
        point_array = np.array([(point.x, point.y, point.z)])
        simplex_array = hull.find_simplex(point_array)
        # The returned array of simplex points is only of length one, as we only query a single point at a time.
        # A value of -1 indicates that no triangle comprising the hull contains the point.
        return simplex_array[0] >= 0

    def contains(self, point: Point) -> bool:
        # from: https://stackoverflow.com/a/43564754
        point = np.array([point.x, point.y, point.z])
        tetrahedron = np.array([[self.a.x, self.a.y, self.a.z], [self.b.x, self.b.y, self.b.z], [self.c.x, self.c.y, self.c.z], [self.d.x, self.d.y, self.d.z]])
        num_coordinates = len(tetrahedron)
        c = np.zeros(num_coordinates)
        A = np.r_[tetrahedron.T, np.ones((1, num_coordinates))]
        b = np.r_[point, np.ones(1)]
        lp = linprog(c, A_eq=A, b_eq=b)
        return lp.success

    def get_longest_side_length(self) -> float:
        if not self._longest_side_len:
            self._longest_side_len = self._calculate_longest_side()
        return self._longest_side_len

    def _calculate_longest_side(self) -> float:
        ab = self.a.distance(self.b)
        ac = self.a.distance(self.c)
        if ab >= ac:
            longest = ab
            e1 = self.a
            e2 = self.b
            n1 = self.c
            n2 = self.d
        else:
            longest = ac
            e1 = self.a
            e2 = self.c
            n1 = self.b
            n2 = self.d
        ad = self.a.distance(self.d)
        if ad > longest:
            longest = ad
            e1 = self.a
            e2 = self.d
            n1 = self.b
            n2 = self.c
        bc = self.b.distance(self.c)
        if bc > longest:
            longest = bc
            e1 = self.b
            e2 = self.c
            n1 = self.a
            n2 = self.d
        bd = self.b.distance(self.d)
        if bd > longest:
            longest = bd
            e1 = self.b
            e2 = self.c
            n1 = self.a
            n2 = self.d
        cd = self.c.distance(self.d)
        if cd > longest:
            longest = cd
            e1 = self.c
            e2 = self.d
            n1 = self.a
            n2 = self.d
        longest_side_len = longest
        self.a = e1
        self.b = e2
        self.c = n1
        self.d = n2
        return longest_side_len

    def subdivide(self) -> typing.Tuple['Tetrahedron', 'Tetrahedron']:
        self.get_longest_side_length()
        # Since calculating the longest side cached the longest edge as A->B, we can split A->B.
        midpoint = self.a.midpoint(self.b)
        tetra_one = Tetrahedron(a=self.a, b=midpoint, c=self.c, d=self.d)
        tetra_two = Tetrahedron(a=midpoint, b=self.b, c=self.c, d=self.d)
        return tetra_one, tetra_two

    def average_altitude(self):
        """
        :return:  Average altitude of the constituent points.
        """
        return (self.a.alt + self.b.alt + self.c.alt + self.d.alt) / 4

    def copy(self):
        """
        :return: New object with new points that have identical coordinates.
        """
        return Tetrahedron(
            a=self.a.copy(),
            b=self.b.copy(),
            c=self.c.copy(),
            d=self.d.copy()
        )

    def __eq__(self, other: 'Tetrahedron') -> bool:
        set_a = {self.a, self.b, self.c, self.d}
        set_b = {other.a, other.b, other.c, other.d}
        return set_a == set_b

    def __hash__(self):
        # TODO: Determine if there is a better way to permit any ordering of the points to result in the same hash.
        total = hash(self.a) + hash(self.b) + hash(self.c) + hash(self.d)
        return hash(total)
