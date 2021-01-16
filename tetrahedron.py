from point import Point
from scipy.spatial import Delaunay
import numpy as np
import typing


class Tetrahedron:

    def __init__(self, a: Point, b: Point, c: Point, d: Point):
        self.a = a
        self.b = b
        self.c = c
        self.d = d
        # TODO: Profile space/time difference of pre-generating a tuple for each point, or just defining them here.
        # Stack overflow suggestion to use scipy Delaunay class: https://stackoverflow.com/a/16898636
        self.hull = Delaunay(np.array([(a.x, a.y, a.z), (b.x, b.y, b.z), (c.x, c.y, c.z), (d.x, d.y, d.z)]))
        self.longest_side_len = None

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

    def contains(self, point: Point) -> bool:
        point_array = np.array([(point.x, point.y, point.z)])
        simplex_array = self.hull.find_simplex(point_array)
        # The returned array of simplex points is only of length one, as we only query a single point at a time.
        # A value of -1 indicates that no triangle comprising the hull contains the point.
        return simplex_array[0] >= 0

    def _calculate_longest_side(self) -> float:
        if not self.longest_side_len:
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
            self.longest_side_len = longest
            # TODO: Is it necessary to copy?  Can I just update the references?
            new_a = e1.copy()
            new_b = e2.copy()
            new_c = n1.copy()
            new_d = n2.copy()
            self.a = new_a
            self.b = new_b
            self.c = new_c
            self.d = new_d
        return self.longest_side_len

    def subdivide(self) -> typing.Tuple['Tetrahedron', 'Tetrahedron']:
        longest_side_len = self._calculate_longest_side()
        # Since calculating the longest side cached the longest edge as A->B, we can split A->B.
        midpoint = self.a.midpoint(self.b)
        tetra_one = Tetrahedron(a=self.a, b=midpoint, c=self.c, d=self.d)
        tetra_two = Tetrahedron(a=midpoint, b=self.b, c=self.c, d=self.d)
        return tetra_one, tetra_two

    def __eq__(self, other: 'Tetrahedron') -> bool:
        set_a = {self.a, self.b, self.c, self.d}
        set_b = {other.a, other.b, other.c, other.d}
        if set_a == set_b:
            return True
        else:
            return False

    def __hash__(self):
        # TODO: Determine if there is a better way to permit any ordering of the points to result in the same hash.
        return hash(hash(self.a) + hash(self.b) + hash(self.c) + hash(self.d))
