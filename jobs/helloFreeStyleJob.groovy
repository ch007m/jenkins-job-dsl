freeStyleJob('hello-dsl-helloFreeStyleJob') {
    displayName('hello-dsl-helloFreeStyleJob')
    description('Job DSL to say hello.')
    label('jnlp')

    steps {
        shell('echo : FreeStyleJob DSL is Saying Hello World !')
    }

}