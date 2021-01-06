
#include "parsers/wavefront_parser.h"

#include <strstream>
#include <iostream>
#include <vector>

#include "data/structs.h"
#include "data/wavefront.h"

struct Face {
    int v[3];
};

static const vec3_getter components[3] = { &Vec3::getx, &Vec3::gety, &Vec3::getz };

#define COND_COMP(i) (i % 2 == 0 ? (special[i]->*comp)() <= (vertex->*comp)() : (special[i]->*comp)() >= (vertex->*comp)())

inline void addToSpec(std::vector<Vec3*>& special, Vec3* vertex, int index, vec3_getter comp){
    if(special[index] == nullptr || COND_COMP(index)){
        if(special[index] != nullptr) delete special[index];
        special[index] = new Vec3(vertex->x, vertex->y, vertex->z);
    }
}

void parseWavefront(JNIEnv* env, jobject obj, jstring data){
    std::strstream stream;
    WavefrontMeshData wvd;

    // Feed the data stream
    stream << env->GetStringUTFChars(data, JNI_FALSE);

    std::vector<Vec3> vertecies, normals;
    std::vector<Vec3*> specials;

    specials.push_back(nullptr);
    specials.push_back(nullptr);
    specials.push_back(nullptr);
    specials.push_back(nullptr);
    specials.push_back(nullptr);
    specials.push_back(nullptr);

    std::vector<Vec2> uvs;

    std::vector<Face> faces;

    // Place default uv & normal coordinates
    uvs.emplace_back();
    normals.emplace_back();

    const int BUFFER_SIZE = 256;
    bool calculateNormals = true;

    // Read the .obj file
    while(true){
        char type;
        // Reads a line from the stream
        char buffer[BUFFER_SIZE];
        stream.getline(buffer, BUFFER_SIZE);

        std::strstream line; line << buffer;

        // Check if the stream is over?
        if(stream.eof()){
            break;
        }


        // Switch vertex type
        line >> type;
        switch (type) {
            case 'v': {
                switch (line.peek()) {
                    case ' ':{  // Model vertex
                        Vec3 vertex;
                        line >> vertex;
                        vertecies.push_back(vertex);

                        // Add to edges
                        for(int i = 0; i < 6; i++)
                            addToSpec(specials, &vertex, i, components[i / 2]);
                    }break;
                    case 't':{
                        line >> type;
                        Vec2 uv;
                        line >> uv;
                        uvs.push_back(uv);
                        // LOGI("UV %f, %f", uv.x, uv.y);
                    }break;
                    case 'n': {   // Mode normal
                        line >> type;
                        Vec3 normal;
                        line >> normal;
                        normals.push_back(normal);
                        // LOGI("Normal %f, %f, %f", normal.x, normal.y, normal.z);
                    }break;
                    default:
                        break;
                }
            }break;
            case 'f': { // Face
                // Pattern should repeat 3 times
                char junk;
                Face face{};
                for(int & i : face.v){
                    int a[3] = {0, 0, 0};
                    line >> a[0];
                    for(int j = 0; j < 2; j++) {
                        if (line.peek() == '/') {
                            line >> junk;
                            if(line.peek() == '/') { a[j + 1] = 0; }
                            else line >> a[j + 1];
                        }
                    }
                    if(a[2] != 0) calculateNormals = false;
                    // LOGI("Face vertex %d v: %d, uv: %d, norm: %d", i, a[0], a[1], a[2]);
                    wvd.put(vertecies[a[0] - 1], uvs[a[1]], normals[a[2]]);
                    i = a[0] - 1;
                }
                faces.push_back(face);
            }break;
                // Something else that is ignored, advance
            default: break;
        }
    }

    // Calculate the normals for undefined surface
    if(calculateNormals){
        for(Face& f : faces){
            Vec3 normal{0,0,0};
            for(int v = 0; v < 3; v++){
                int nv = (v + 1) % 3;
                int nnv = (v + 2) % 3;
                Vec3 e1 = vertecies[nv] - vertecies[v],
                        e2 = vertecies[nnv] - vertecies[nv];
                normal += cross(e1, e2);
            }
            normal.normalize();
            wvd.appendNormal(f.v[0], normal);
            wvd.appendNormal(f.v[1], normal);
            wvd.appendNormal(f.v[2], normal);
        }

        wvd.normalizeNormals();
    }

    wvd.createJObject(env, obj, specials);
}

