#!/usr/bin/env bash
# Stamps the "Unreleased" section of the behavior-changes page with the version
# being released, and inserts a fresh empty "Unreleased" section above it.
#
# Called by the release workflow (release.yml) on the release branch, before
# maven-release-plugin tags the release, so the released site and the
# post-release pull request back to the default branch both carry the change.
#
# If the "Unreleased" section records no behavior changes (no bullet items),
# the file is left untouched: the page omits releases without user-visible
# behavior changes.
set -euo pipefail

version="${1:?usage: stamp-behavior-changes.sh <releaseVersion>}"
file="src/site/markdown/behavior-changes.md"
releaseDate="$(date -u +%Y-%m-%d)"
placeholder="_No user-visible behavior changes recorded yet._"

if [ ! -f "${file}" ]; then
	echo "ERROR: ${file} not found." >&2
	exit 1
fi

if ! grep -qx "Unreleased" "${file}"; then
	echo "ERROR: ${file} has no 'Unreleased' section; restore it before releasing." >&2
	exit 1
fi

# Count bullet items inside the "Unreleased" section (up to the next setext heading).
bullets="$(awk '
	{ lines[NR] = $0 }
	END {
		inSection = 0
		count = 0
		for (i = 1; i <= NR; i++) {
			if (!inSection && lines[i] == "Unreleased" && lines[i + 1] ~ /^-{4,}$/) {
				inSection = 1
				i++
				continue
			}
			if (inSection) {
				if (i < NR && lines[i] != "" && lines[i + 1] ~ /^-{4,}$/) {
					break
				}
				if (lines[i] ~ /^- /) {
					count++
				}
			}
		}
		print count
	}
' "${file}")"

if [ "${bullets}" -eq 0 ]; then
	echo "No behavior changes recorded under 'Unreleased'; leaving ${file} unchanged."
	exit 0
fi

# Replace the "Unreleased" heading line with the version heading (reusing the
# existing setext underline), insert a fresh "Unreleased" stub above it, and
# drop the placeholder left by a previous stamping from the released section.
tmp="$(mktemp)"
awk -v version="${version}" -v releaseDate="${releaseDate}" -v placeholder="${placeholder}" '
	$0 == "Unreleased" && !stamped {
		print "Unreleased"
		print "----------"
		print ""
		print placeholder
		print ""
		printf "[v%s](https://github.com/jawkio/jawk/releases/tag/v%s) (%s)\n", version, version, releaseDate
		stamped = 1
		next
	}
	stamped && $0 == placeholder {
		skipBlank = 1
		next
	}
	skipBlank && $0 == "" {
		skipBlank = 0
		next
	}
	{
		skipBlank = 0
		print
	}
' "${file}" > "${tmp}"
mv "${tmp}" "${file}"

if ! grep -qF "[v${version}](https://github.com/jawkio/jawk/releases/tag/v${version}) (${releaseDate})" "${file}"; then
	echo "ERROR: failed to stamp v${version} in ${file}." >&2
	exit 1
fi

echo "Stamped ${bullets} behavior change(s) as v${version} (${releaseDate}) in ${file}."
