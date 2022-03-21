import React, { useEffect } from "react";

export interface Item {
  code: string;
  outOfStock: boolean;
  name: string;
  price: number;
  quantity: number;
}
export const cart: Array<Item> = [
  {
    code: "tomato",
    outOfStock: false,
    name: "토마토",
    price: 7000,
    quantity: 2,
  },
  {
    code: "orange",
    outOfStock: true,
    name: "오렌지",
    price: 15000,
    quantity: 3,
  },
  {
    code: "apple",
    outOfStock: false,
    name: "사과",
    price: 10000,
    quantity: 2,
  },
];
export const CartList = () => {
  let html = "";
  let totalCount = 0;

  for (let i = 0; i < cart.length; i++) {
    html += "<li>";
    html += `<h2>${cart[i].name}</h2>`;
    html += `<div>가격: ${cart[i].price}원</div>`;
    html += `<div>수량: ${cart[i].quantity}상자</div>`;
    html += "</li>";
    totalCount += cart[i].quantity;
  }

  return [html, totalCount];
};

const PrintCart: React.FC = () => {
  useEffect(() => {
    const mainBody = document.getElementById("main-body");
    if (mainBody != null) {
      const [info, totalCount] = CartList();
      mainBody.innerHTML = `<h1>장바구니</h1>
      <ul>${info}</ul>
      <h2>전체 수량: ${totalCount}</h2>`;
    }
  });
  return <></>;
};

export default PrintCart;