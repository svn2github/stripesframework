package net.sourceforge.stripes.test;

import java.util.List;
import java.util.Map;

/**
 * A simple JavaBean that has all sorts of properties, making it good
 * for testing out pieces of Stripes.
 *
 * @author Tim Fennell
 */
public class TestBean {
    private String stringProperty;
    private int intProperty;
    private Long longProperty;
    private TestEnum enumProperty;

    private String[] stringArray;
    private List<String> stringList;
    private Map<String,String> stringMap;

    private List<TestBean> beanList;
    private Map<String,TestBean> beanMap;

    private TestBean nestedBean;

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public int getIntProperty() {
        return intProperty;
    }

    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }

    public Long getLongProperty() {
        return longProperty;
    }

    public void setLongProperty(Long longProperty) {
        this.longProperty = longProperty;
    }

    public TestEnum getEnumProperty() {
        return enumProperty;
    }

    public void setEnumProperty(TestEnum enumProperty) {
        this.enumProperty = enumProperty;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public Map<String, String> getStringMap() {
        return stringMap;
    }

    public void setStringMap(Map<String, String> stringMap) {
        this.stringMap = stringMap;
    }

    public TestBean getNestedBean() {
        return nestedBean;
    }

    public void setNestedBean(TestBean nestedBean) {
        this.nestedBean = nestedBean;
    }

    public List<TestBean> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<TestBean> beanList) {
        this.beanList = beanList;
    }

    public Map<String, TestBean> getBeanMap() {
        return beanMap;
    }

    public void setBeanMap(Map<String, TestBean> beanMap) {
        this.beanMap = beanMap;
    }
}
