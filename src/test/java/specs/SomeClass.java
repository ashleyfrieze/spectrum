package specs;

/**
 * Depends on {@link SomeInterface}
 */
public class SomeClass {
	private SomeInterface someInterface;

	public SomeClass(SomeInterface someInterface) {
		this.someInterface = someInterface;
	}

	public String getResult() {
		return someInterface.getInput();
	}
}
