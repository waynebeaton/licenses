package org.eclipse.dash.licenses.context;

import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.foundation.EclipseFoundationSupport;
import org.eclipse.dash.licenses.http.HttpClientService;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.npmjs.ExtendedContentDataService;
import org.eclipse.dash.licenses.npmjs.IExtendedContentDataProvider;
import org.eclipse.dash.licenses.npmjs.MavenCentralExtendedContentDataProvider;
import org.eclipse.dash.licenses.npmjs.NpmjsExtendedContentDataProvider;
import org.eclipse.dash.licenses.review.GitLabSupport;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class LicenseToolModule extends AbstractModule {

	private ISettings settings;

	public LicenseToolModule(ISettings settings) {
		this.settings = settings;
	}

	@Override
	protected void configure() {
		bind(ISettings.class).toInstance(settings);
		bind(LicenseChecker.class).toInstance(new LicenseChecker());
		bind(EclipseFoundationSupport.class).toInstance(new EclipseFoundationSupport());
		bind(ClearlyDefinedSupport.class).toInstance(new ClearlyDefinedSupport());
		bind(LicenseSupport.class).toInstance(new LicenseSupport());
		bind(GitLabSupport.class).toInstance(new GitLabSupport());
		bind(IHttpClientService.class).toInstance(new HttpClientService());
		bind(LicenseSupport.class).toInstance(new LicenseSupport());
		bind(ExtendedContentDataService.class).toInstance(new ExtendedContentDataService());

		Multibinder<IExtendedContentDataProvider> classifierBinder = Multibinder.newSetBinder(binder(),
				IExtendedContentDataProvider.class);
		classifierBinder.addBinding().to(NpmjsExtendedContentDataProvider.class);
		classifierBinder.addBinding().to(MavenCentralExtendedContentDataProvider.class);
		// classifierBinder.addBinding().to(GithubDataProvider.class);
	}
}