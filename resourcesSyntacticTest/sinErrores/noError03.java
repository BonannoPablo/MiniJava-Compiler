/// [SinError]
abstract class A<B> extends C{

}
class A extends B<C>{

}
interface A extends B{}
interface A<B> extends B<C>{}
static interface A{}