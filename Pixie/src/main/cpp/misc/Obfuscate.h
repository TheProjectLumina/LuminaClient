/* --------------------------------- ABOUT -------------------------------------
Original Author: Adam Yaxley
Website: https://github.com/adamyaxley
License: See end of file
Obfuscate
Guaranteed compile-time string literal obfuscation library for C++14
Usage:
Pass string literals into the AY_OBFUSCATE macro to obfuscate them at compile
time. AY_OBFUSCATE returns a reference to an ay::obfuscated_data object with the
following traits:
  - Guaranteed obfuscation of string
  The passed string is encrypted with a simple XOR cipher at compile-time to
  prevent it being viewable in the binary image
  - Global lifetime
  The actual instantiation of the ay::obfuscated_data takes place inside a
  lambda as a function level static
  - Implicitly convertable to a char*
  This means that you can pass it directly into functions that would normally
  take a char* or a const char*
Example:
const char* obfuscated_string = AY_OBFUSCATE("Hello World");
std::cout << obfuscated_string << std::endl;
----------------------------------------------------------------------------- */
#ifndef OBF_H
#define OBF_H
#include <cstddef>
#include <string>
//======================================================================================================================
#ifndef AY_OBFUSCATE_DEFAULT_KEY



#define AY_OBFUSCATE_DEFAULT_KEY ay::generate_key(__LINE__)
#endif
//======================================================================================================================
namespace ay
{
    using size_type = unsigned long long;
    using key_type = unsigned long long;


    constexpr key_type generate_key(key_type seed)
    {

        key_type key = seed;
        key ^= (key >> 33);
        key *= 0xff51afd7ed558ccd;
        key ^= (key >> 33);
        key *= 0xc4ceb9fe1a85ec53;
        key ^= (key >> 33);


        key |= 0x0101010101010101ull;

        return key;
    }


    constexpr void cipher(char* data, size_type size, key_type key)
    {

        for (size_type i = 0; i < size; i++)
        {
            data[i] ^= char(key >> ((i % 8) * 8));
        }
    }


    template <size_type N, key_type KEY>
    class obfuscator
    {
    public:

        constexpr obfuscator(const char* data)
        {

            for (size_type i = 0; i < N; i++)
            {
                m_data[i] = data[i];
            }



            cipher(m_data, N, KEY);
        }

        constexpr const char* data() const
        {
            return &m_data[0];
        }

        constexpr size_type size() const
        {
            return N;
        }

        constexpr key_type key() const
        {
            return KEY;
        }

    private:

        char m_data[N]{};
    };


    template <size_type N, key_type KEY>
    class obfuscated_data
    {
    public:
        obfuscated_data(const obfuscator<N, KEY>& obfuscator)
        {

            for (size_type i = 0; i < N; i++)
            {
                m_data[i] = obfuscator.data()[i];
            }
        }

        ~obfuscated_data()
        {

            for (size_type i = 0; i < N; i++)
            {
                m_data[i] = 0;
            }
        }



        operator char*()
        {
            decrypt();
            return m_data;
        }

        operator std::string()
        {
            decrypt();
            return m_data;
        }


        void decrypt()
        {
            if (m_encrypted)
            {
                cipher(m_data, N, KEY);
                m_encrypted = false;
            }
        }


        void encrypt()
        {
            if (!m_encrypted)
            {
                cipher(m_data, N, KEY);
                m_encrypted = true;
            }
        }


        bool is_encrypted() const
        {
            return m_encrypted;
        }

    private:



        char m_data[N];


        bool m_encrypted{ true };
    };



    template <size_type N, key_type KEY = AY_OBFUSCATE_DEFAULT_KEY>
    constexpr auto make_obfuscator(const char(&data)[N])
    {
        return obfuscator<N, KEY>(data);
    }
}
//======================================================================================================================



#define OBFUSCATE(data) OBFUSCATE_KEY(data, AY_OBFUSCATE_DEFAULT_KEY)
//======================================================================================================================




#define OBFUSCATE_KEY(data, key) \
	[]() -> ay::obfuscated_data<sizeof(data)/sizeof(data[0]), key>& { \
		static_assert(sizeof(decltype(key)) == sizeof(ay::key_type), "key must be a 64 bit unsigned integer"); \
		static_assert((key) >= (1ull << 56), "key must span all 8 bytes"); \
		constexpr auto n = sizeof(data)/sizeof(data[0]); \
		constexpr auto obfuscator = ay::make_obfuscator<n, key>(data); \
		static auto obfuscated_data = ay::obfuscated_data<n, key>(obfuscator); \
		return obfuscated_data; \
	}()
#endif
/* -------------------------------- LICENSE ------------------------------------
Public Domain (http://www.unlicense.org)
This is free and unencumbered software released into the public domain.
Anyone is free to copy, modify, publish, use, compile, sell, or distribute this
software, either in source code form or as a compiled binary, for any purpose,
commercial or non-commercial, and by any means.
In jurisdictions that recognize copyright laws, the author or authors of this
software dedicate any and all copyright interest in the software to the public
domain. We make this dedication for the benefit of the public at large and to
the detriment of our heirs and successors. We intend this dedication to be an
overt act of relinquishment in perpetuity of all present and future rights to
this software under copyright law.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
----------------------------------------------------------------------------- */