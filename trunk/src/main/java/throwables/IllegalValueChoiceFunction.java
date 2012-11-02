package throwables;

public class IllegalValueChoiceFunction extends Error {
	
	private static final long serialVersionUID = 1L;

	public IllegalValueChoiceFunction(String vcf, String domainName) {
		super("ValueFunction " + vcf + " not defined for domain " + domainName);
	}	
}
