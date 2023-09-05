interface StackData<T> {
  [index: number]: T;
}

class Stack<T> {
  private items: StackData<T> = {};
  private count = 0;

  push(ele: T) {
    this.items[this.count++] = ele;
  }

  // 如果堆栈中已存在元素ele，则将其升至栈顶
  pushElement(ele: T) {
    let isFound = false;
    const newItems: StackData<T> = {};
    let k = 0;
    for (let i = 0; i < this.count; i++) {
      if (this.items[i] == ele) {
        // 重新排序，将ele移至栈顶
        isFound = true;
      } else {
        newItems[k] = this.items[i];
        k++;
      }
    }

    if (!isFound) {
      this.items[this.count++] = ele;
      console.log('this.items1', this.items);
    } else {
      this.items = newItems;
      this.items[k] = ele;
      this.count = k + 1;
      console.log('this.items2', this.items);
    }
  }

  peek() {
    return this.items[this.count - 1];
  }

  pop() {
    if (this.isEmpty()) return;
    const result = this.items[--this.count];
    delete this.items[this.count];
    return result;
  }

  popElement(ele: T) {
    if (this.isEmpty()) return;

    if (this.items[this.count - 1] == ele) {
      const result = this.items[--this.count];
      console.log('popElement result', result);
      delete this.items[this.count];
      return result;
    } else {
      let isFound = false;
      const newItems: StackData<T> = {};
      let k = 0;
      for (let i = 0; i < this.count; i++) {
        if (this.items[i] == ele) {
          isFound = true;
        } else {
          newItems[k] = this.items[i];
          k++;
        }
      }

      if (isFound) {
        this.items = newItems;
        this.count = k;
        console.log('popElement result2', ele);
        return ele;
      } else {
        console.warn('popElement Form: ' + ele + ' is not found');
        return null;
      }
    }
  }

  isEmpty() {
    return this.count === 0;
  }

  size() {
    return this.count;
  }

  clear() {
    this.items = {};
    this.count = 0;
  }

  toString() {
    if (this.isEmpty()) return '';
    let objString = `${this.items[0]}`;
    for (let i = 1; i < this.count; i++) {
      objString += ` ${this.items[i]}`;
    }
    return objString;
  }
}

export default Stack;

/*
// 用法
import { Stack } from '/@/utils/stack';

const stack = new Stack<string>();
// 入栈
stack.push('第一条数据');
stack.push('第二条数据');
// 出栈
stack.pop();
// 返回栈顶元素
console.log(stack.peek());
// 查看栈大小
console.log(stack.size());
// 判断栈是否为空
console.log(stack.isEmpty());
// 返回栈内所有元素
console.log(stack.toString());
// 清空栈
stack.clear();
 */
