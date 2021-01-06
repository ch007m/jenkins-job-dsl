package org.mytools

class Tools implements Serializable {
    private static final long serialVersionUID
    def steps

    Tools(steps) {
        this.steps = steps
    }

    void myEcho(String msg) {
        steps.echo msg
    }
}