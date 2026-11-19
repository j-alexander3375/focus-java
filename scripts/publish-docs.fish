#!/usr/bin/env fish
# Regenerates Javadoc and publishes it to the gh-pages branch.

set docs ../focus-docs
set rev (git rev-parse --short HEAD)

mvn -q -B javadoc:javadoc; or begin
    echo "Javadoc generation failed"
    exit 1
end

rsync -a --delete --exclude .git --exclude .nojekyll target/reports/apidocs/ $docs/
touch $docs/.nojekyll

cd $docs
git add -A
if git diff --cached --quiet
    echo "Docs unchanged, nothing to publish"
    exit 0
end
git commit -m "Update Javadoc from $rev"
git push