package test;

import it.unisa.dia.gas.jpbc.Element;

import java.io.Serializable;

/**
 * Created by admin on 2020/6/6.
 */
public class TestElement implements Serializable {
    private Element element;

    public TestElement() {

    }

    public TestElement(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }
}
