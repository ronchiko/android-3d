#pragma once

#include <istream>

struct Vec2 {
    float x, y;

    Vec2() : x(0), y(0) {}
};
struct Vec3 {
    float x, y, z;

    Vec3() : x(0), y(0), z(0) {}
    Vec3(float x, float y, float z) : x(0), y(0), z(0) {}

    inline float getx() const noexcept { return x; }
    inline float gety() const noexcept { return y; }
    inline float getz() const noexcept { return z; }

    Vec3 operator+(const Vec3&) const noexcept ;
    Vec3 operator-(const Vec3&) const noexcept ;

    Vec3& operator+=(const Vec3&) noexcept;
    Vec3& operator-=(const Vec3&) noexcept;

    inline float magnitude() const;

    void normalize();
};

std::istream& operator>>(std::istream&, Vec3&);
std::istream& operator>>(std::istream&, Vec2&);

Vec3 cross(const Vec3&, const Vec3& b);

typedef float (Vec3::*vec3_getter)() const;