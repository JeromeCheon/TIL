'use strict';

// ๊ธฐ๋ณธ Array ๐ API๋ค 

// 1. ์ ์ธ
const arr1 = new Array();
const arr2 = [1, 2];
console.clear();

// 4. Addition, deletion, copy
const fruits = ['๐', '๐']; // ์ด๋ชจ์ง๋ค์ ๋ฌธ์์ด๋ก ์ทจ๊ธ
// push: add an item to the end
fruits.push('๐', '๐');
console.log(fruits);

// pop: remove an item from the end
fruits.pop();
fruits.pop();
console.log(fruits);
// unshift: add an item to the beginning
fruits.unshift('๐', '๐ฅ');
console.log(fruits);
// shift remove an item from the beginning
fruits.shift();
// ๊ทผ๋ฐ shift ๋ unshift๋ pop, push ๋ณด๋ค ๋ง์ด ๋๋ ค 
console.log(fruits);
// splice: remove an item by index position
// fruits.splice(1, 1); // ๋๋ฒ์งธ๋ฅผ ์๋ ฅํ์ง ์์ผ๋ฉด ๊ทธ ๋ค ๋ด์ฉ์ ์ ๋ถ ์ง์ฐ๊ฒ ๋ค๋ ์๋ฏธ

// 5. Searching
// find the index
console.clear();
console.log(fruits);
console.log(fruits.indexOf('๐'));

// includes
console.log(fruits.includes('๐'));

// lastIndexOf
// ๋งจ ๋ง์ง๋ง์ ๋์ค๋ ์ธ๋ฑ์ค์ ๊ฐ์ ๋ฐ์์จ๋ค. 

///////////////////////////////////////////////
