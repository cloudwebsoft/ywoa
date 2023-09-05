import Stack from '/@/utils/stack';

// 用于维护当前正在处理的form id
class CurFormUtil {
  private stack: Stack<string> = new Stack<string>();
  private map: Map<string, Array<number>> = new Map();
  private formNo = 0;

  addInterval(intervalId: number, formId = '') {
    let ary: Array<number> | undefined;
    if (formId) {
      ary = this.map.get(formId);
      if (!ary) {
        console.warn(
          'addInterval formId: ' +
            formId +
            ' is not found in map. Have you use curFormUtil.set(...)?',
        );
        ary = new Array<number>();
        this.map.set(formId, ary);
      }
    } else {
      ary = this.map.get(this.get());
      if (!ary) {
        ary = new Array<number>();
        this.map.set(this.get(), ary);
      }
    }

    ary.push(intervalId);
    console.log('addInterval formId:', this.get(), ary);
  }

  setInterval(func: Function, timeout: number) {
    const sint = window.setInterval(func, timeout);
    this.addInterval(sint);
  }

  clearIntervals(formId = '') {
    if (formId == '') {
      console.log('clearIntervals formId', this.get());
      const ary: Array<number> | undefined = this.map.get(this.get());
      console.log('formId', formId, 'clearIntervals', ary);
      if (ary) {
        for (const i in ary) {
          // console.log('clearIntervals', ary[i]);
          window.clearInterval(ary[i]);
        }
      }
    } else {
      const ary: Array<number> | undefined = this.map.get(formId);
      console.log('formId', formId, 'clearIntervals2', ary);
      if (ary) {
        for (const i in ary) {
          // console.log('clearIntervals', ary[i]);
          window.clearInterval(ary[i]);
        }
      }
    }
  }

  get() {
    // console.log('curFormUtil get: ' + this.stack.peek());
    return this.stack.peek();
  }

  getFormNo() {
    this.formNo++;
    return this.formNo;
  }

  set(formId: string) {
    console.log('curFormUtil set formId', formId);
    this.stack.pushElement(formId);
  }

  // 在unmounted时close
  close(formId = '') {
    destroyCurrentFormObj();

    if (!formId) {
      console.log('curFormUtil close: ' + this.get());
      this.clearIntervals();
      this.stack.pop();
    } else {
      console.log('curFormUtil close2: ' + formId);
      this.clearIntervals(formId);
      this.stack.popElement(formId);
    }
  }
}

export default CurFormUtil;
